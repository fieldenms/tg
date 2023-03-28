package ua.com.fielden.platform.processors.generate;

import static java.util.Optional.ofNullable;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

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
import ua.com.fielden.platform.processors.annotation.ProcessedValue;
import ua.com.fielden.platform.processors.exceptions.ProcessorInitializationException;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

/**
 * An annotation processor that generates and maintains the {@code ApplicationDomain} class, which implements {@link IApplicationDomainProvider}.
 * <p>
 * The processor's behaviour depends on the existence of previously generated {@code ApplicationDomain}:
 * <ul>
 *   <li>Doesn't exist -- simply generates it from scratch</li>
 *   <li>Exists -- might need to regenerate it considering the following:</li>
 *   <ul>
 *     <li>Register new domain entity types</li>
 *     <li>Remove entity types that cannot be located any more (e.g., due to removal of the java source)</li>
 *     <li>Remove entity types that are no longer domain entities</li>
 *   </ul>
 * </ul>
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
    private ApplicationDomainFinder appDomainFinder;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.appDomainFinder = new ApplicationDomainFinder(entityFinder);
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

        final Set<EntityElement> entities = roundEnv.getRootElements().stream()
            .filter(elt -> entityFinder.isEntityType(elt.asType()))
            .map(elt -> entityFinder.newEntityElement((TypeElement) elt))
            .filter(this::isDomainEntity)
            .collect(Collectors.toSet());

        // removal of a registered entity will cause recompilation of ApplicationDomain, so we need to check if it's among root elements
        final Optional<ApplicationDomainElement> maybeAppDomainRootElt = findApplicationDomainInRound(roundEnv);

        // no input entities and no input ApplicationDomain => this change does not affect ApplicationDomain
        if (entities.isEmpty() && maybeAppDomainRootElt.isEmpty()) {
            printNote("There is nothing to do.");
            return false;
        }

        // if ApplicationDomain is not among root elements, then search through the whole environment
        final Optional<ApplicationDomainElement> maybeAppDomainElt = maybeAppDomainRootElt.isEmpty() ?
                findApplicationDomain() : maybeAppDomainRootElt.map(elt -> new ApplicationDomainElement(elt, entityFinder));

        if (maybeAppDomainElt.isPresent()) {
            printNote("Found existing %s", maybeAppDomainElt.get().getSimpleName());
            regenerate(entities, maybeAppDomainElt.get());
        } else {
            printNote("%s hasn't been generated yet.", APPLICATION_DOMAIN_SIMPLE_NAME);
            generate(entities);
        }

        // TODO save the effort of regeneration if all root entities are already registered

        return false;
    }

    private boolean isDomainEntity(final EntityElement entity) {
        return !ElementFinder.isAbstract(entity.element());
    }

    private void generate(final Collection<EntityElement> entities) {
        printNote("Generating %s from scratch", APPLICATION_DOMAIN_SIMPLE_NAME);
        writeApplicationDomain(entities);
    }

    private void regenerate(final Collection<EntityElement> entities, final ApplicationDomainElement appDomainElt) {
        final List<EntityElement> unchanged = new LinkedList<>();
        final List<EntityElement> toBeUnregistered = new LinkedList<>();
        appDomainElt.entities().forEach(ent -> {
            if (isDomainEntity(ent)) {
                unchanged.add(ent);
            } else {
                toBeUnregistered.add(ent);
            }
        });

        printNote("Previously registered entities: %s unchanged, %s missing, %s to be unregistered",
                unchanged.size(), appDomainElt.errorTypes().size(), toBeUnregistered.size());

        final Set<EntityElement> toRegister = new HashSet<>();
        toRegister.addAll(unchanged);
        toRegister.addAll(entities);
        writeApplicationDomain(toRegister);
    }

    private void writeApplicationDomain(final Collection<EntityElement> registeredEntities) {
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
            return bld;
        };

        // class-level @Generated annotation
        final AnnotationSpec atGenerated = AnnotationSpec.builder(ClassName.get(Generated.class))
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime)
                .build();

        // We use @ProcessedValue annotations just to enable the processor to get access to the list of registered types,
        // since we can't analyse the insides of the static initialiser block.
        // This approach lends itself well to refactoring. When entity types are renamed, ApplicationDomain will be automatically adjusted,
        // because class literals are used to refer to registered entity types.

         // @ProcessedValue(cls = $ENTITY_TYPE)...
        final List<AnnotationSpec> processedValueAnnots = registeredEntities.stream()
                .map(entity -> AnnotationSpec.builder(ProcessedValue.class)
                        .addMember("cls", CodeBlock.of("$T.class", entity.element()))
                        .build())
                .toList();
        /*
         * @Generated(...)
         * @ProcessedValue(...)...
         * public class ApplicationDomain implements IApplicationDomainProvider
         */
        final TypeSpec typeSpec = TypeSpec.classBuilder(APPLICATION_DOMAIN_SIMPLE_NAME)
            .addModifiers(PUBLIC)
            .addSuperinterface(IApplicationDomainProvider.class)
            .addAnnotation(atGenerated)
            .addAnnotations(processedValueAnnots)
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

        printNote("Generated %s with %s registered entities.", getApplicationDomainQualifiedName(), registeredEntities.size());
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

}
