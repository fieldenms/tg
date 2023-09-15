package ua.com.fielden.platform.processors.appdomain;

import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static ua.com.fielden.platform.processors.ProcessorOptionDescriptor.parseOptionFrom;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.asTypeElementOfTypeMirror;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;
import javax.tools.Diagnostic.Kind;

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
import ua.com.fielden.platform.processors.ProcessorOptionDescriptor;
import ua.com.fielden.platform.processors.appdomain.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.appdomain.annotation.RegisteredEntity;
import ua.com.fielden.platform.processors.appdomain.annotation.SkipEntityRegistration;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.CollectionUtil;

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
 * To exclude application-level entity types from registration, annotation {@link SkipEntityRegistration} should be used.
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
    /**
     * The <i>basename</i> of the generated {@code ApplicationDomain}, i.e., the last component of the package name.
     */
    public static final String APPLICATION_DOMAIN_PKG_BASENAME = "config";

    public static final String ERR_AT_MOST_ONE_EXTENSION_POINT_IS_ALLOWED = "At most one extension point is allowed.";

    private ElementFinder elementFinder;
    private EntityFinder entityFinder;

    // initialised after parsing options
    private String applicationDomainPackage;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.applicationDomainPackage = "%s.%s".formatted(packageName, APPLICATION_DOMAIN_PKG_BASENAME);
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

        // 3. input extension
        final Optional<ExtendApplicationDomain.Mirror> inputExtension = findApplicationDomainExtensionInRound(roundEnv);

        // this is an incremental build, but it doesn't affect us
        if (inputEntities.isEmpty() && maybeAppDomainRootElt.isEmpty() && inputExtension.isEmpty()) {
            printNote("There is nothing to do.");
            return false;
        }

        // if ApplicationDomain is not among root elements, then search through the whole environment
        final Optional<ApplicationDomainElement> maybeAppDomainElt = maybeAppDomainRootElt.isEmpty() ?
                findApplicationDomain() : maybeAppDomainRootElt.map(elt -> new ApplicationDomainElement(elt, entityFinder));

        maybeAppDomainElt.ifPresentOrElse(elt -> {
            // incremental build <=> regenerate
            printNote("Found existing %s (%s registered entities)", elt.getSimpleName(), elt.entities().size() + elt.externalEntities().size());
            regenerate(elt, inputEntities, inputExtension);
        }, /*else*/ () -> {
            // generate from scratch
            printNote("%s hasn't been generated yet.", APPLICATION_DOMAIN_SIMPLE_NAME);
            generate(inputEntities, inputExtension);
        });

        return false;
    }

    private boolean isDomainEntity(final EntityElement entity) {
        return !ElementFinder.isAbstract(entity.element());
    }

    private boolean shouldSkipRegistration(final EntityElement entity) {
        return entity.getAnnotation(SkipEntityRegistration.class) != null;
    }

    private void generate(final Collection<EntityElement> inputEntities, final Optional<ExtendApplicationDomain.Mirror> inputExtension) {
        printNote("Generating %s from scratch", APPLICATION_DOMAIN_SIMPLE_NAME);

        final List<EntityElement> toRegister = inputEntities.stream()
                .filter(ent -> isDomainEntity(ent) && !shouldSkipRegistration(ent))
                .toList();

        final List<EntityElement> externalEntities = inputExtension.map(mirr -> streamEntitiesFromExtension(mirr).toList())
                .orElseGet(() -> List.of());

        if (!externalEntities.isEmpty()) {
            printNote("Found %s entities from extensions.".formatted(externalEntities.size()));
        }
        writeApplicationDomain(toRegister, externalEntities);
    }

    private void regenerate(
            final ApplicationDomainElement appDomainElt,
            final Collection<EntityElement> inputEntities, final Optional<ExtendApplicationDomain.Mirror> inputExtension)
    {
        final Set<EntityElement> toUnregister = new HashSet<>();
        final Set<EntityElement> toRegister = new HashSet<>();

        // analyse input entities
        // * domain entities -- are there any new ones we need to register?
        toRegister.addAll(inputEntities.stream()
                .filter(this::isDomainEntity)
                .filter(ent -> !appDomainElt.entities().contains(ent))
                .toList());
        // * non-domain entities OR skipped ones -- were any of them registered? (we need to unregister them)
        toUnregister.addAll(inputEntities.stream()
                .filter(ent -> !isDomainEntity(ent) || shouldSkipRegistration(ent))
                .filter(ent -> appDomainElt.entities().contains(ent))
                .toList());

        // analyse the input extension if it exists
        final Set<EntityElement> externalEntities = inputExtension.map(mirr -> streamEntitiesFromExtension(mirr).collect(toSet()))
                .orElseGet(() -> Set.of());
        // NOTE: we assume that external entities are always domain entities, thus don't perform additional checks
        // * are there any new external entities we need to register?
        final List<EntityElement> externalToRegister = externalEntities.stream()
                .filter(ent -> !appDomainElt.entities().contains(ent))
                .toList();
        // * are there any external entities that need to be unregistered?
        // only if an extension has been modified
        final List<EntityElement> externalToUnregister = inputExtension.isEmpty() ? List.of() :
            appDomainElt.externalEntities().stream()
                .filter(ent -> !externalEntities.contains(ent))
                .toList();

        // analyse ApplicationDomain
        // are there any missing entity types (e.g., due to removal)?
        final List<ErrorType> missingRegisteredTypes = appDomainElt.errorTypes();

        if (!toRegister.isEmpty()) {
            printNote("Registering (%s): [%s]".formatted(
                    toRegister.size(),
                    CollectionUtil.toString(toRegister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
        }
        if (!externalToRegister.isEmpty()) {
            printNote("Registering external (%s): [%s]".formatted(
                    externalToRegister.size(),
                    CollectionUtil.toString(externalToRegister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
        }
        if (!toUnregister.isEmpty()) {
            printNote("Unregistering (%s): [%s]".formatted(
                    toUnregister.size(), CollectionUtil.toString(toUnregister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
        }
        if (!externalToUnregister.isEmpty()) {
            printNote("Unregistering external (%s): [%s]".formatted(
                    externalToUnregister.size(),
                    CollectionUtil.toString(externalToUnregister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
        }
        if (!missingRegisteredTypes.isEmpty()) {
            printNote("Missing (%s): [%s]".formatted(missingRegisteredTypes.size(), CollectionUtil.toString(missingRegisteredTypes, ", ")));
        }

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

        final JavaFile javaFile = JavaFile.builder(applicationDomainPackage, typeSpec).indent("    ").build();
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
        return "%s.%s".formatted(applicationDomainPackage, APPLICATION_DOMAIN_SIMPLE_NAME);
    }

    protected Optional<ApplicationDomainElement> findApplicationDomain() {
        return elementFinder.findTypeElement(getApplicationDomainQualifiedName()).map(elt -> new ApplicationDomainElement(elt, entityFinder));
    }

    public static Optional<ApplicationDomainElement> findApplicationDomain(ProcessingEnvironment procEnv, EntityFinder entityFinder) {
        final String qualName = "%s.%s.%s".formatted(parseOptionFrom(procEnv.getOptions(), PACKAGE_OPT_DESC),
                APPLICATION_DOMAIN_PKG_BASENAME, APPLICATION_DOMAIN_SIMPLE_NAME);
        return entityFinder.findTypeElement(qualName).map(elt -> new ApplicationDomainElement(elt, entityFinder));
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
     * Returns an {@link Optional} describing the first encountered {@link ExtendApplicationDomain} annotation in the given round.
     * @param roundEnv
     * @return
     */
    protected Optional<ExtendApplicationDomain.Mirror> findApplicationDomainExtensionInRound(final RoundEnvironment roundEnv) {
        final List<? extends Element> extensions = roundEnv.getRootElements().stream()
                .filter(elt -> elt.getAnnotation(ExtendApplicationDomain.class) != null)
                .toList();

        if (extensions.size() > 1) {
            extensions.forEach(elt -> messager.printMessage(Kind.ERROR, ERR_AT_MOST_ONE_EXTENSION_POINT_IS_ALLOWED, elt));
        }

        return extensions.stream()
                .map(elt -> ExtendApplicationDomain.Mirror.from(elt.getAnnotation(ExtendApplicationDomain.class), entityFinder))
                .findFirst();
    }

    protected Stream<EntityElement> streamEntitiesFromExtension(final ExtendApplicationDomain.Mirror mirror) {
        return mirror.entities().stream() // stream of Mirror instances of @RegisterEntity
                // map to EntityElement
                .map(atRegEntityMirror -> entityFinder.newEntityElement(asTypeElementOfTypeMirror(atRegEntityMirror.value())));
    }

}
