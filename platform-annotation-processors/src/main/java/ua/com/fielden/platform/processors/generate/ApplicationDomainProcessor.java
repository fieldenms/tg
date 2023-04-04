package ua.com.fielden.platform.processors.generate;

import static java.util.Optional.ofNullable;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;

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

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.exceptions.ProcessorInitializationException;
import ua.com.fielden.platform.processors.generate.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.generate.annotation.RegisterEntity;
import ua.com.fielden.platform.processors.generate.annotation.RegisteredEntity;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

/**
 * An annotation processor that generates and maintains the {@code ApplicationDomain} class, which implements {@link IApplicationDomainProvider}.
 * <p>
 * The following sources of information are taken into account during processing:
 * <ol>
 *   <li>Set of input entities.</li>
 *   <li>Previously generated {@code ApplicationDomain}.</li>
 *   <li>Extensions, i.e., types annotated with {@link ExtendApplicationDomain}. Generally, there should be a single such type.</li>
 * </ol>
 *
 * <p>
 * The maintenance of the generated {@code ApplicationDomain} is carried out according to the following rules:
 * <ul>
 *  <li>New domain entity types are incrementally registered.</li>
 *  <li>Registered entity types that cannot be located any more (e.g., due to removal of the java source) are deregistered.</li>
 *  <li>Registered entity types that no longer wish to be registered or are structurally modified in such a way that they are no longer
 *      domain entity types are deregistered.</li>
 * </ul>
 *
 * Renaming of java sources by means of the IDE refactoring capabilites should automatically lead to the respective renaming in the generated
 * {@code ApplicationDomain}.
 * <p>
 *
 * <h3>Registration of 3rd-party entities</h3>
 * 3rd-party entities are those that come from dependencies. Their registration requires a designated application-level class that
 * must be annotated with {@link ExtendApplicationDomain}, which shall be used to specify them.
 *
 * @author TG Team
 */
@SupportedAnnotationTypes("*")
public class ApplicationDomainProcessor extends AbstractPlatformAnnotationProcessor {

    public static final String APPLICATION_DOMAIN_SIMPLE_NAME = "ApplicationDomain";

    public static final String PACKAGE_OPTION = "packageName";
    private String packageName = "generated.config";

    private ElementFinder elementFinder;
    private EntityFinder entityFinder;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    }

    @Override
    public Set<String> getSupportedOptions() {
        final Set<String> options = new HashSet<>(super.getSupportedOptions());
        options.add(PACKAGE_OPTION);
        return options;
    }

    @Override
    protected void parseOptions(final Map<String, String> options) {
        super.parseOptions(options);

        ofNullable(options.get(PACKAGE_OPTION)).ifPresent(pkg -> {
            if (!Pattern.matches("([a-zA-Z]\\w*\\.)*[a-zA-Z]\\w*", pkg)) {
                throw new ProcessorInitializationException("Option [%s] specifies an illegal package name.".formatted(PACKAGE_OPTION));
            }
            this.packageName = pkg;
        });
    }

    @Override
    protected boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        // if this is an incremental build, then any newly created entity types will be passed to the first round
        // otherwise it's a full build and all sources will passed to the first round
        // therefore, we do not care about further rounds
        if (getRoundNumber() > 1) {
            return false;
        }

        // Gather information sources.
        // We look only at input elements to determine the kind of build being performed (incremental / full) with respect to ApplicationDomain
        // (regenerate / generate from scratch).

        // 1. input entities (all of them, not just domain ones)
        final Set<EntityElement> inputEntities = roundEnv.getRootElements().stream()
            .filter(elt -> entityFinder.isEntityType(elt.asType()))
            .map(elt -> entityFinder.newEntityElement((TypeElement) elt))
            .collect(Collectors.toSet());

        // 2. previously generated ApplicationDomain
        // removal of a registered entity will cause recompilation of ApplicationDomain
        final Optional<ApplicationDomainElement> maybeAppDomainRootElt = findApplicationDomainInRound(roundEnv);

        // 3. input extensions
        final List<ExtendApplicationDomain.Mirror> inputExtensions = findApplicationDomainExtensionsInRound(roundEnv);

        // this is an incremental build, but it doesn't affect us
        if (inputEntities.isEmpty() && maybeAppDomainRootElt.isEmpty() && inputExtensions.isEmpty()) {
            printNote("There is nothing to do.");
            return false;
        }

        // if ApplicationDomain is not among root elements, then search through the whole environment
        final Optional<ApplicationDomainElement> maybeAppDomainElt = maybeAppDomainRootElt.isEmpty() ?
                findApplicationDomain() : maybeAppDomainRootElt.map(elt -> new ApplicationDomainElement(elt, entityFinder));

        if (maybeAppDomainElt.isPresent()) {
            // incremental build <=> regenerate
            printNote("Found existing %s", maybeAppDomainElt.get().getSimpleName());
            regenerate(maybeAppDomainElt.get(), inputEntities, inputExtensions);
        } else {
            // generate from scratch
            printNote("%s hasn't been generated yet.", APPLICATION_DOMAIN_SIMPLE_NAME);
            generate(inputEntities, inputExtensions);
        }

        return false;
    }

    private boolean isDomainEntity(final EntityElement entity) {
        return !ElementFinder.isAbstract(entity.element());
    }

    private void generate(final Collection<EntityElement> inputEntities, final Collection<ExtendApplicationDomain.Mirror> inputExtensions) {
        printNote("Generating %s from scratch", APPLICATION_DOMAIN_SIMPLE_NAME);

        final List<EntityElement> inputDomainEntities = inputEntities.stream().filter(this::isDomainEntity).toList();

        final Set<EntityElement> extensionEntities = inputExtensions.stream()
            .flatMap(mirr -> mirr.entities().stream()) // stream of Mirror instances of @RegisterEntity
            .map(RegisterEntity.Mirror::value) // map to EntityElement
            .collect(Collectors.toSet());

        if (!extensionEntities.isEmpty()) {
            printNote("Found %s entities from extensions.".formatted(extensionEntities.size()));
        }
        writeApplicationDomain(inputDomainEntities, extensionEntities);
    }

    private void regenerate(
            final ApplicationDomainElement appDomainElt,
            final Collection<EntityElement> inputEntities, final Collection<ExtendApplicationDomain.Mirror> inputExtensions)
    {
        final Set<EntityElement> toUnregister = new HashSet<>();
        final Set<EntityElement> toRegister = new HashSet<>();

        // analyse input entities
        // * domain entities -- are there any new ones we need to register?
        toRegister.addAll(inputEntities.stream()
                .filter(this::isDomainEntity)
                .filter(ent -> !appDomainElt.entities().contains(ent))
                .toList());
        // * non-domain entities -- were any of them registered? (we need to unregister them)
        toUnregister.addAll(inputEntities.stream()
                .filter(Predicate.not(this::isDomainEntity))
                .filter(ent -> appDomainElt.entities().contains(ent))
                .toList());

        // analyse input extensions
        final Set<EntityElement> externalEntities = inputExtensions.stream()
            .flatMap(mirr -> mirr.entities().stream()) // stream of Mirror instances of @RegisterEntity
            .map(RegisterEntity.Mirror::value) // map to EntityElement
            .collect(Collectors.toSet());
        // * are there any new external entities we need to register?
        final List<EntityElement> externalToRegister = externalEntities.stream()
                .filter(ent -> !appDomainElt.entities().contains(ent))
                .toList();
        // * are there any external entities that need to be unregistered?
        final List<EntityElement> externalToUnregister = appDomainElt.externalEntities().stream()
                .filter(ent -> !externalEntities.contains(ent))
                .toList();

        // analyse ApplicationDomain
        // are there any missing entity types (e.g., due to removal)?
        final List<ErrorType> missingRegisteredTypes = appDomainElt.errorTypes();

        // consider if we actually need to regenerate
        if (!missingRegisteredTypes.isEmpty() || !toUnregister.isEmpty() || !externalToUnregister.isEmpty() // anything to exclude?
                || !toRegister.isEmpty() || !externalToRegister.isEmpty()) {  // anything to include?
            printNote("Regenerating %s", appDomainElt.getSimpleName());

            final Set<EntityElement> registeredEntities = new HashSet<>(appDomainElt.entities());
            registeredEntities.removeAll(toUnregister);
            registeredEntities.addAll(toRegister);

            final Set<EntityElement> externalRegisteredEntities = new HashSet<>(appDomainElt.externalEntities());
            externalRegisteredEntities.removeAll(externalToUnregister);
            externalRegisteredEntities.addAll(externalToRegister);

            writeApplicationDomain(registeredEntities, externalRegisteredEntities);
        }
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
            for (final var entity: externalEntities) {
                bld = bld.addStatement("add($T.class)", ClassName.get(entity.element()));
            }
            return bld;
        };

        // class-level @Generated annotation
        final AnnotationSpec atGenerated = AnnotationSpec.builder(ClassName.get(Generated.class))
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime)
                .build();

        // We use @RegisteredEntity annotations just to enable the processor to get access to the list of registered types,
        // since we can't analyse the insides of the static initialiser block.
        // This approach lends itself well to refactoring. When entity types are renamed, ApplicationDomain will be automatically adjusted,
        // because class literals are used to refer to registered entity types.

         // @RegisteredEntity($ENTITY.class)...
        final List<AnnotationSpec> registeredEntityAnnots = new ArrayList<>(registeredEntities.size() + externalEntities.size());
        registeredEntityAnnots.addAll(registeredEntities.stream()
                .map(entity -> AnnotationSpec.builder(RegisteredEntity.class)
                        .addMember("value", "$T.class", entity.element())
                        .build())
                .toList());

         // @RegisteredEntity($ENTITY.class, external = true)...
        registeredEntityAnnots.addAll(externalEntities.stream()
                .map(entity -> AnnotationSpec.builder(RegisteredEntity.class)
                        .addMember("value", "$T.class", entity.element())
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

        final JavaFile javaFile = JavaFile.builder(packageName, typeSpec).indent("    ").build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            printError("Failed to generate %s: %s\n%s", getApplicationDomainQualifiedName(), ex.getMessage(), ExceptionUtils.getStackTrace(ex));
            return;
        }

        final int totalRegistered = registeredEntities.size() + externalEntities.size();
        printNote("Generated %s with %s registered entities.", getApplicationDomainQualifiedName(), totalRegistered);
    }

    protected String getApplicationDomainQualifiedName() {
        return "%s.%s".formatted(packageName, APPLICATION_DOMAIN_SIMPLE_NAME);
    }

    protected Optional<ApplicationDomainElement> findApplicationDomain() {
        return elementFinder.findTypeElement(getApplicationDomainQualifiedName()).map(elt -> new ApplicationDomainElement(elt, entityFinder));
    }

    protected Optional<ApplicationDomainElement> findApplicationDomainInRound(final RoundEnvironment roundEnv) {
        return roundEnv.getRootElements().stream()
                .filter(elt -> elt.getKind() == ElementKind.CLASS)
                .map(elt -> (TypeElement) elt)
                .filter(elt -> elt.getQualifiedName().contentEquals(getApplicationDomainQualifiedName()))
                .findFirst()
                .map(elt -> new ApplicationDomainElement(elt, entityFinder));
    }

    /**
     * Returns a list of modelled instances of {@link ExtendApplicationDomain} annotation.
     * @param roundEnv
     * @return
     */
    protected List<ExtendApplicationDomain.Mirror> findApplicationDomainExtensionsInRound(final RoundEnvironment roundEnv) {
        return roundEnv.getRootElements().stream()
                .map(elt -> ExtendApplicationDomain.Mirror.fromAnnotated(elt, entityFinder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    protected List<EntityElement> collectExternalEntitiesInRound(final RoundEnvironment roundEnv) {
//        return roundEnv.getRootElements().stream()
//                .map(elt -> RegisterEntity.Mirror.fromAnnotated(elt, elementFinder))
//                .filter(Optional::isPresent)
//                // extract the TypeMirror instances representing the external entities from annotations
//                .flatMap(annotMirror -> annotMirror.get().values().stream())
//                .map(typeMirror -> entityFinder.newEntityElement(ElementFinder.asTypeElementOfTypeMirror(typeMirror)))
//                .toList();
        // FIXME
        return List.of();
    }

}
