package ua.com.fielden.platform.processors.appdomain;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.DateTimeUtils;
import ua.com.fielden.platform.processors.ProcessorOptionDescriptor;
import ua.com.fielden.platform.processors.appdomain.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.appdomain.annotation.RegisteredEntity;
import ua.com.fielden.platform.processors.exceptions.ProcessorInitializationException;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.processors.ProcessorOptionDescriptor.parseOptionFrom;

/// An annotation processor that generates and maintains the `ApplicationDomain` class, which implements [IApplicationDomainProvider].
///
/// The following sources of information are taken into account during processing:
///
/// - Set of input entities.
/// - Previously generated `ApplicationDomain`.
/// - Extensions, i.e., types annotated with [ExtendApplicationDomain]. Generally, there should be a single such type.
///
/// The maintenance of the generated `ApplicationDomain` is carried out according to the following rules:
///
/// - New domain entity types are incrementally registered.
/// - Registered entity types that cannot be located any more (e.g., due to removal of the java source) are unregistered.
/// - Registered entity types that no longer wish to be registered or are structurally modified in such a way that they are no longer domain entity types are unregistered.
///
/// Renaming of java sources by means of IDE refactoring capabilities should automatically lead to the adjustment of `ApplicationDomain`.
///
/// To exclude application-level entity types from registration, annotation [SkipEntityRegistration] should be used.
///
/// #### Registration of 3rd-party entities
/// External, 3rd-party entities are those that come from dependencies.
/// Their registration requires a specific application-level class to be annotated with [ExtendApplicationDomain], listing external entity types.
/// The established convention it to use class `fielden.config.ApplicationConfig` in the `pojo-bl` module,
/// although this can be customised with option {@linkplain #APP_DOMAIN_EXTENSION_OPT_DESC appDomainExtension}.
/// Any other annotated classes will be ignored.
///
/// #### Supported options
///
/// - [appDomainPkg][#APP_DOMAIN_PKG_OPT_DESC] - destination package of a generated `ApplicationDomain`
/// - [appDomainExtension][#APP_DOMAIN_EXTENSION_OPT_DESC] - fully-qualified name of the `ApplicationDomain` [extension][ExtendApplicationDomain].
///
@SupportedAnnotationTypes("*")
public class ApplicationDomainProcessor extends AbstractPlatformAnnotationProcessor {

    public static final String APPLICATION_DOMAIN_SIMPLE_NAME = "ApplicationDomain";
    public static final String ERR_AT_MOST_ONE_EXTENSION_POINT_IS_ALLOWED = "At most one extension point is allowed.";

    private final static String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private final static Pattern FQCN_OR_PKG_NAME_PATTERN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");


    public static final ProcessorOptionDescriptor<String> APP_DOMAIN_PKG_OPT_DESC = new ProcessorOptionDescriptor<>() {
        @Override public String name() { return "appDomainPkg"; }
        @Override public String defaultValue() { return "fielden.config"; }

        @Override public String parse(String value) {
            if (!FQCN_OR_PKG_NAME_PATTERN.matcher(value).matches()) {
                throw new ProcessorInitializationException("Option [%s] specifies an illegal package name [%s]."
                        .formatted(name(), value));
            }
            return value;
        }
    };

    public static final ProcessorOptionDescriptor<String> APP_DOMAIN_EXTENSION_OPT_DESC = new ProcessorOptionDescriptor<>() {
        @Override public String name() { return "appDomainExtension"; }
        @Override public String defaultValue() { return "fielden.config.ApplicationConfig"; }

        @Override public String parse(String value) {
            if (!FQCN_OR_PKG_NAME_PATTERN.matcher(value).matches()) {
                throw new ProcessorInitializationException("Option [%s] specifies an illegal class name [%s]."
                        .formatted(name(), value));
            }
            return value;
        }
    };

    /// Returns the fully-qualified name of the `ApplicationDomain` class determined by given processor options.
    ///
    public static String getApplicationDomainFqn(Map<String, String> procOptions) {
        return "%s.%s".formatted(parseOptionFrom(procOptions, ApplicationDomainProcessor.APP_DOMAIN_PKG_OPT_DESC), APPLICATION_DOMAIN_SIMPLE_NAME);
    }

    private String appDomainPkg;
    private String appDomainFqn;

    RegisteredEntitiesCollector registeredEntitiesCollector;
    private ElementFinder elementFinder;
    private EntityFinder entityFinder;

    /// Populated in each round.
    private final Set<EntityElement> allInputEntities = new HashSet<>();
    /// Assigned only in the 1st round.
    private Optional<ExtendApplicationDomainMirror> maybeInputExtension = Optional.empty();
    /// Assigned only in the 1st round.
    private Optional<ApplicationDomainElement> maybeAppDomainRootElt = Optional.empty();

    @Override
    public Set<String> getSupportedOptions() {
        return Stream.concat(super.getSupportedOptions().stream(),
                        Stream.of(APP_DOMAIN_PKG_OPT_DESC, APP_DOMAIN_EXTENSION_OPT_DESC).map(ProcessorOptionDescriptor::name))
                .collect(toSet());
    }

    @Override
    protected void parseOptions(Map<String, String> options) {
        super.parseOptions(options);
        appDomainPkg = parseOptionFrom(options, APP_DOMAIN_PKG_OPT_DESC);
        appDomainFqn = getApplicationDomainFqn(options);
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.registeredEntitiesCollector = RegisteredEntitiesCollector.getInstance(processingEnv);
        this.elementFinder = new ElementFinder(processingEnv);
        this.entityFinder = new EntityFinder(processingEnv);
    }

    @Override
    protected boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        // Entities should be collected from the 1st round (initial inputs) and the 2nd round (generated entities).
        final var pair = registeredEntitiesCollector.scanRoundInputs(roundEnv);
        allInputEntities.addAll(pair.getKey());

        if (getRoundNumber() == 1) {
            // The extension class can only appear among the inputs of the 1st round, it cannot be generated by other processors.
            maybeInputExtension = pair.getValue();
            // Removal of a registered entity will cause recompilation of ApplicationDomain.
            maybeAppDomainRootElt = registeredEntitiesCollector.findApplicationDomainInRound(roundEnv);
        }

        if (getRoundNumber() == 2) {
            // this is an incremental build, but it doesn't affect us
            if (allInputEntities.isEmpty() && maybeAppDomainRootElt.isEmpty() && maybeInputExtension.isEmpty()) {
                printNote("There is nothing to do.");
                return false;
            }

            // if ApplicationDomain is not among root elements, then search through the whole environment
            final Optional<ApplicationDomainElement> maybeAppDomainElt = maybeAppDomainRootElt
                    .map(elt -> new ApplicationDomainElement(elt, entityFinder))
                    .or(registeredEntitiesCollector::findApplicationDomain);
            maybeAppDomainElt.ifPresentOrElse(
                    (elt) -> {
                        // incremental build <=> regenerate
                        printNote("Found existing %s (%s registered entities)", elt.getSimpleName(),
                                elt.entities().size() + elt.externalEntities().size());
                    },
                    // or else
                    () -> {
                        printNote("Generating %s from scratch.", APPLICATION_DOMAIN_SIMPLE_NAME);
                    });

            final var registerableEntities = new TreeSet<EntityElement>();
            final var registerableExtEntities = new TreeSet<EntityElement>();
            final boolean merged = registeredEntitiesCollector.mergeRegisteredEntities(allInputEntities,
                                                                                       maybeInputExtension,
                                                                                       maybeAppDomainElt,
                                                                                       registerableEntities::add,
                                                                                       registerableExtEntities::add);
            if (merged) {
                writeApplicationDomain(registerableEntities, registerableExtEntities);
            }
            else {
                printNote("There is nothing to do.");
            }
        }

        return false;
    }

    private void writeApplicationDomain(final Collection<EntityElement> registeredEntities, final Collection<EntityElement> externalEntities) {
        final ParameterizedTypeName classExtendsAbstractEntity = ParameterizedTypeName.get(
                ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(ParameterizedTypeName.get(
                        ClassName.get(AbstractEntity.class), WildcardTypeName.subtypeOf(Object.class))));

        // used below in the static initialisation block
        final Function<CodeBlock.Builder, CodeBlock.Builder> addStatementsForRegisteredEntities = builder -> {
            var bld = builder;
            for (final var entity: registeredEntities) {
                bld = bld.addStatement("add($T.class)", ClassName.get(entity.element()));
            }
            if (!externalEntities.isEmpty()) {
                bld.add("///////////////////////\n");
                bld.add("// External Entities //\n");
                bld.add("///////////////////////\n");
            }
            for (final var entity: externalEntities) {
                bld = bld.addStatement("add($T.class)", ClassName.get(entity.element()));
            }
            return bld;
        };

        // @Generated annotation
        final String dateString = DateTimeUtils.toIsoFormat(DateTimeUtils.zonedNow());
        final AnnotationSpec atGenerated = buildAtGenerated(dateString);

        // We use @RegisteredEntity annotations just to enable the processor to get access to the list of registered types,
        // since we can't analyse the insides of the static initialiser block.
        // This approach lends itself well to refactoring. When entity types are renamed, ApplicationDomain will be automatically adjusted,
        // because class literals are used to refer to registered entity types.

         // @RegisteredEntity($ENTITY.class)...
        final List<AnnotationSpec> registeredEntityAnnots = new ArrayList<>(registeredEntities.size() + externalEntities.size());
        registeredEntityAnnots.addAll(registeredEntities.stream()
                .map(entity -> AnnotationSpec.builder(RegisteredEntity.class)
                        .addMember("value", "$T.class", ClassName.get(entity.element()))
                        .build())
                .toList());

         // @RegisteredEntity($ENTITY.class, external = true)...
        registeredEntityAnnots.addAll(externalEntities.stream()
                .map(entity -> AnnotationSpec.builder(RegisteredEntity.class)
                        .addMember("value", "$T.class", ClassName.get(entity.element()))
                        .addMember("external", "$L", true)
                        .build())
                .toList());

        /*
         * @Generated(...)
         * @RegisteredEntity(...)...
         * public class ApplicationDomain implements IApplicationDomainProvider
         */
        final TypeSpec typeSpec = TypeSpec.classBuilder(APPLICATION_DOMAIN_SIMPLE_NAME)
            .addModifiers(PUBLIC)
            .addSuperinterface(IApplicationDomainProvider.class)
            .addAnnotation(atGenerated)
            .addAnnotations(registeredEntityAnnots)
            // private static final Set<Class<? extends AbstractEntity<?>>> entityTypes = new LinkedHashSet<>();
            .addField(FieldSpec.builder(
                    ParameterizedTypeName.get(ClassName.get(Set.class), classExtendsAbstractEntity),
                    "entityTypes",
                    PRIVATE, STATIC, FINAL)
                    .initializer("new $T<>()", LinkedHashSet.class)
                    .build())
            // private static final Set<Class<? extends AbstractEntity<?>>> domainTypes = new LinkedHashSet<>();
            .addField(FieldSpec.builder(
                    ParameterizedTypeName.get(ClassName.get(Set.class), classExtendsAbstractEntity),
                    "domainTypes",
                    PRIVATE, STATIC, FINAL)
                    .initializer("new $T<>()", LinkedHashSet.class)
                    .build())
            /*
             * private static void add(final Class<? extends AbstractEntity<?>> domainType) {
             *     entityTypes.add(domainType);
             *     domainTypes.add(domainType);
             * }
             */
            .addMethod(MethodSpec.methodBuilder("add")
                    .addModifiers(PRIVATE, STATIC)
                    .returns(void.class)
                    .addParameter(classExtendsAbstractEntity, "domainType", FINAL)
                    .addStatement("entityTypes.add(domainType)")
                    .addStatement("domainTypes.add(domainType)")
                    .build())
            /*
             * static {
             *     entityTypes.addAll(PlatformDomainTypes.types);
             *     for each registered entity type:
             *          add($ENTITY_TYPE.class);
             * }
             */
            .addStaticBlock(addStatementsForRegisteredEntities.apply(
                    CodeBlock.builder()
                    .addStatement("entityTypes.addAll($T.types)", PlatformDomainTypes.class))
                    .build())
            /*
             * @Override
             * public List<Class<? extends AbstractEntity<?>>> entityTypes() {
             *     return entityTypes.stream().collect(Collectors.toUnmodifiableList());
             * }
             */
            .addMethod(MethodSpec.methodBuilder("entityTypes")
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(List.class), classExtendsAbstractEntity))
                    .addStatement("return entityTypes.stream().collect($T.toUnmodifiableList())", Collectors.class)
                    .build())
            /*
             * public static List<Class<? extends AbstractEntity<?>>> domainTypes() {
             *     return domainTypes.stream().collect(Collectors.toUnmodifiableList());
             * }
             */
            .addMethod(MethodSpec.methodBuilder("domainTypes")
                    .addModifiers(PUBLIC, STATIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(List.class), classExtendsAbstractEntity))
                    .addStatement("return domainTypes.stream().collect($T.toUnmodifiableList())", Collectors.class)
                    .build())
            .build();

        final JavaFile javaFile = JavaFile.builder(appDomainPkg, typeSpec).indent("    ").build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            printError("Failed to generate %s: %s%n%s", appDomainFqn, ex.getMessage(), ExceptionUtils.getStackTrace(ex));
            return;
        }

        final int totalRegistered = registeredEntities.size() + externalEntities.size();
        printNote("Generated %s with %s registered entities.", appDomainFqn, totalRegistered);
    }

}
