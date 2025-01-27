package ua.com.fielden.platform.audit;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.squareup.javapoet.*;
import jakarta.inject.Inject;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Optional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.*;
import static ua.com.fielden.platform.audit.AbstractAuditProp.AUDIT_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditProp.PROPERTY;
import static ua.com.fielden.platform.audit.AnnotationSpecs.compositeKeyMember;
import static ua.com.fielden.platform.audit.AnnotationSpecs.synAuditFor;
import static ua.com.fielden.platform.audit.AuditUtils.getAuditEntityTypeVersion;
import static ua.com.fielden.platform.audit.AuditUtils.isAuditProperty;
import static ua.com.fielden.platform.audit.PropertySpec.propertyBuilder;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.*;
import static ua.com.fielden.platform.utils.StreamUtils.distinct;

final class AuditEntityGeneratorImpl implements AuditEntityGenerator {

    private final IDomainMetadata domainMetadata;
    private final GeneratorEnvironment environment;
    private final JavaPoet javaPoet;
    private final IAuditTypeFinder auditTypeFinder;

    @Inject
    AuditEntityGeneratorImpl(final IDomainMetadata domainMetadata, final IAuditTypeFinder auditTypeFinder) {
        this.domainMetadata = domainMetadata;
        this.auditTypeFinder = auditTypeFinder;
        environment = new GeneratorEnvironment();
        javaPoet = environment.javaPoet();
    }

    @Override
    public Collection<Path> generate(
            final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            final Path sourceRoot,
            final VersionStrategy versionStrategy)
    {
        entityTypes.forEach(AuditEntityGeneratorImpl::validateAuditedType);
        return Streams.stream(entityTypes)
                .parallel()
                .flatMap(type -> {
                    final var result = generate_(type, versionStrategy);
                    final Path auditEntityPath;
                    final Path auditPropPath;
                    try {
                        auditEntityPath = result.auditEntity.writeToPath(sourceRoot);
                        auditPropPath = result.auditProp.writeToPath(sourceRoot);
                    } catch (final IOException e) {
                        throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(result.auditVersion, type.getTypeName()), e);
                    }
                    return Stream.of(auditEntityPath, auditPropPath);
                })
                .collect(toImmutableSet());
    }

    @Override
    public Set<SourceInfo> generateSources(
            final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            final VersionStrategy versionStrategy)
    {
        class $ {
            static SourceInfo makeSourceInfo(final JavaFile javaFile) {
                final var className = javaFile.packageName.isEmpty() ? javaFile.typeSpec.name : javaFile.packageName + '.' + javaFile.typeSpec.name;
                return new SourceInfo(className, javaFile.toString());
            }
        }

        entityTypes.forEach(AuditEntityGeneratorImpl::validateAuditedType);
        return Streams.stream(entityTypes)
                .parallel()
                .flatMap(type -> {
                    final var result = generate_(type, versionStrategy);
                    return Stream.of($.makeSourceInfo(result.auditEntity), $.makeSourceInfo(result.auditProp));
                })
                .collect(toImmutableSet());
    }

    private static void validateAuditedType(final Class<? extends AbstractEntity<?>> auditedType) {
        if (!AuditUtils.isAudited(auditedType)) {
            throw new InvalidArgumentException(format("Entity type [%s] cannot be audited. It must be annotated with @%s.",
                                                      auditedType.getTypeName(), Audited.class.getTypeName()));
        }
    }

    private LocalResult generate_(
            final Class<? extends AbstractEntity<?>> type,
            final VersionStrategy versionStrategy)
    {
        final var lastAuditTypeVersion = auditTypeFinder.findAuditEntityType(type).map(AuditUtils::getAuditEntityTypeVersion);
        final var newAuditTypeVersion = switch (versionStrategy) {
            case NEW -> lastAuditTypeVersion.map(v -> v + 1).orElse(1);
            case OVERWRITE_LAST -> lastAuditTypeVersion.orElse(1);
        };

        final var auditTypePkg = type.getPackageName();
        final var auditTypeName = type.getSimpleName() + "_" + A3T + "_" + newAuditTypeVersion;
        final var auditPropTypeName = auditTypeName + "_Prop";

        final AuditEntitySpec auditEntitySpec;
        final JavaFile auditProp;
        try {
            auditEntitySpec = generateAuditEntity(type, auditTypePkg, auditTypeName,
                                                  ClassName.get(auditTypePkg, auditPropTypeName),
                                                  newAuditTypeVersion);
            auditProp = generateAuditPropEntity(type, auditEntitySpec.className(), auditTypePkg, auditPropTypeName);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(newAuditTypeVersion, type.getTypeName()), e);
        }

        return new LocalResult(auditEntitySpec.javaFileBuilder().build(), auditProp, newAuditTypeVersion);
    }

    record LocalResult(JavaFile auditEntity, JavaFile auditProp, int auditVersion) {}

    private AuditEntitySpec generateAuditEntity(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName,
            final ClassName auditPropTypeName,
            final int auditTypeVersion)
    {
        final var auditTypeClassName = ClassName.get(auditTypePkg, auditTypeName);

        final var a3tBuilder = new AuditEntitySpecBuilder(auditTypeClassName, type, auditTypeVersion);

        a3tBuilder.addAnnotation(AnnotationSpecs.entityTitle("%s Audit".formatted(getEntityTitle(type))));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(SkipVerification.class));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class));

        // Property for the reference to the audited entity.
        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var auditedEntityTitle = getEntityTitle(type);
        final var auditedEntityProp = propertyBuilder(AUDITED_ENTITY, type)
                .addAnnotation(compositeKeyMember(AUDITED_ENTITY_KEY_MEMBER_ORDER))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(javaPoet.getAnnotation(Required.class))
                .addAnnotation(javaPoet.getAnnotation(Final.class))
                .addAnnotation(AnnotationSpecs.title(auditedEntityTitle, "The audited %s.".formatted(auditedEntityTitle)))
                .build();

        // Collectional property to model one-to-many association with the audit-prop entity
        final var changedPropsProp = propertyBuilder(CHANGED_PROPS, ParameterizedTypeName.get(javaPoet.getClassName(Set.class), auditPropTypeName))
                .addAnnotation(AnnotationSpecs.title("Changed Properties", "Properties changed as part of the audit event."))
                .initializer("new $T<>()", javaPoet.getClassName(HashSet.class))
                .build();

        a3tBuilder.addProperty(auditedEntityProp);
        a3tBuilder.addProperty(changedPropsProp);

        // Audited properties
        final var auditedEntityMetadata = domainMetadata.forEntity(type);
        auditedEntityMetadata.properties().stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .filter(AuditEntityGeneratorImpl::isAudited)
                .map(pm -> {
                    final var propBuilder = propertyBuilder(AuditUtils.auditPropertyName(pm.name()),
                                                            pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, type, pm.name())))
                            .addAnnotation(
                                    AnnotationSpecs.mapTo((AuditUtils.auditPropertyName(pm.name())).toUpperCase()))
                            .addAnnotation(javaPoet.getAnnotation(Final.class));

                    final var propTitle = getTitleAndDesc(pm.name(), type).getKey();
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(AnnotationSpecs.title(propTitle, "[%s] at the time of the audited event.".formatted(propTitle)));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        return a3tBuilder.build(addSkipEntityExistsValidation);
    }

    private AnnotationSpec mkIsPropertyForAudit(final IsProperty isProperty) {
        final var builder = AnnotationSpec.builder(IsProperty.class);

        if (isProperty.value() != IsProperty.DEFAULT_VALUE) {
            builder.addMember("value", "$T.class", isProperty.value());
        }
        if (!Objects.equals(isProperty.linkProperty(), IsProperty.DEFAULT_LINK_PROPERTY)) {
            builder.addMember("linkProperty", "$S", isProperty.linkProperty());
        }
        // assignBeforeSave is ignored, its semantics should not be applied to audit-entities
        if (isProperty.length() != IsProperty.DEFAULT_LENGTH) {
            builder.addMember("length", "$L", isProperty.length());
        }
        if (isProperty.precision() != IsProperty.DEFAULT_PRECISION) {
            builder.addMember("precision", "$L", isProperty.precision());
        }
        if (isProperty.scale() != IsProperty.DEFAULT_SCALE) {
            builder.addMember("scale", "$L", isProperty.scale());
        }
        if (isProperty.trailingZeros() != IsProperty.DEFAULT_TRAILING_ZEROS) {
            builder.addMember("trailingZeros", "$L", isProperty.trailingZeros());
        }
        if (!Objects.equals(isProperty.displayAs(), IsProperty.DEFAULT_DISPLAY_AS)) {
            builder.addMember("displayAs", "$S", isProperty.displayAs());
        }

        return builder.build();
    }

    private static <A extends Annotation> A requirePropertyAnnotation(
            final Class<A> annotationType,
            final Class<?> enclosingType,
            final CharSequence propertyPath)
    {
        return requireNonNull(getPropertyAnnotation(annotationType, enclosingType, propertyPath.toString()),
                              () -> "Missing annotation @%s on property [%s] in [%s]"
                                      .formatted(annotationType.getSimpleName(), propertyPath, enclosingType.getSimpleName()));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : AuditEntityBuilder.Processor declarations
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Annotates each entity-typed property with {@link SkipEntityExistsValidation}, unless this annotation is already present.
     */
    private final AuditEntitySpecBuilder.Processor addSkipEntityExistsValidation = new AuditEntitySpecBuilder.Processor() {
        public PropertySpec processProperty(final AuditEntitySpecBuilder builder, final PropertySpec propSpec) {
            return javaPoet.reflectType(propSpec.type()) instanceof Class klass
                   && domainMetadata.forEntityOpt(klass).isPresent()
                   && !propSpec.hasAnnotation(javaPoet.getClassName(SkipEntityExistsValidation.class))
                    ? propSpec.toBuilder().addAnnotation(javaPoet.getAnnotation(SkipEntityExistsValidation.class)).build()
                    : propSpec;
        }
    };

    private static boolean isAudited(final PropertyMetadata.Persistent property) {
        return !AuditEntityGenerator.NON_AUDITED_PROPERTIES.contains(property.name());
    }

    private record AuditEntitySpec
            (ClassName className,
             TypeSpec typeSpec,
             List<PropertySpec> properties,
             int version)
    {

        public JavaFile.Builder javaFileBuilder() {
            return JavaFile.builder(className.packageName(), typeSpec);
        }

        public boolean hasProperty(final PropertySpec property) {
            return properties.stream().anyMatch(p -> p.name().equals(property.name()) && p.type().equals(property.type()));
        }

    }

    private AuditEntitySpec newAuditEntitySpec(final Class<? extends AbstractAuditEntity<?>> auditType) {
        final var properties = Finder.streamRealProperties(auditType)
                .map(prop -> propertyBuilder(prop.getName(), prop.getGenericType())
                        .addAnnotations(Arrays.stream(prop.getAnnotations()).map(AnnotationSpec::get).toList())
                        // Initializer is not available and also not needed
                        .build())
                .toList();
        final var className = ClassName.get(auditType);
        final var typeSpecBuilder = classBuilder(className)
                .addModifiers(PUBLIC)
                .superclass(auditType.getSuperclass())
                .addAnnotations(Arrays.stream(auditType.getAnnotations()).map(AnnotationSpec::get).toList());
        properties.forEach(p -> addPropertyTo(p, typeSpecBuilder, className));
        return new AuditEntitySpec(className, typeSpecBuilder.build(), properties, getAuditEntityTypeVersion(auditType));
    }

    class AuditEntitySpecBuilder {
        final ClassName className;
        final Class<? extends AbstractEntity<?>> auditedType;
        final ArrayList<PropertySpec> properties;
        final ArrayList<MethodSpec> methods;
        final ArrayList<AnnotationSpec> annotations;
        final int auditVersion;

        private AuditEntitySpecBuilder(
                final ClassName className,
                final Class<? extends AbstractEntity<?>> auditedType,
                final int auditVersion)
        {
            this.className = className;
            this.auditedType = auditedType;
            this.auditVersion = auditVersion;
            this.properties = new ArrayList<>();
            this.methods = new ArrayList<>();
            this.annotations = new ArrayList<>();
        }

        public AuditEntitySpec build(final Processor processor) {
            final var builder = classBuilder(className)
                    .addModifiers(PUBLIC)
                    .superclass(ParameterizedTypeName.get(AbstractAuditEntity.class, auditedType))
                    .addAnnotation(AnnotationSpecs.auditFor(auditedType, auditVersion))
                    // TODO Meta-model is not needed. Meta-model processor needs to support a new annotation - WithoutMetaModel.
                    .addAnnotation(MapEntityTo.class)
                    .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class))
                    .addAnnotations(annotations);
            properties.stream()
                    .map(prop -> processor.processProperty(this, prop))
                    .forEach(propSpec -> {
                        builder.addField(propSpec.toFieldSpec(environment));
                        builder.addMethod(propSpec.getAccessorSpec(environment));
                        builder.addMethod(propSpec.getSetterSpec(environment, className));
                    });
            builder.addMethods(methods);
            final var typeSpec = builder.build();

            return new AuditEntitySpec(className, typeSpec, properties, auditVersion);}

        public AuditEntitySpecBuilder addProperty(final PropertySpec property) {
            properties.add(property);
            return this;
        }

        public AuditEntitySpecBuilder addProperties(final PropertySpec... properties) {
            for (final var property : properties) {
                this.properties.add(property);
            }
            return this;
        }

        public AuditEntitySpecBuilder addProperties(final Iterable<? extends PropertySpec> properties) {
            properties.forEach(this.properties::add);
            return this;
        }

        public AuditEntitySpecBuilder addMethod(MethodSpec method) {
            methods.add(method);
            return this;
        }

        public AuditEntitySpecBuilder addAnnotation(final AnnotationSpec annotation) {
            annotations.add(annotation);
            return this;
        }

        interface Processor {
            PropertySpec processProperty(AuditEntitySpecBuilder builder, PropertySpec property);
        }
    }

    private TypeSpec.Builder addPropertyTo(final PropertySpec propSpec, final TypeSpec.Builder builder, final ClassName builderClassName) {
        return builder.addField(propSpec.toFieldSpec(environment))
                .addMethod(propSpec.getAccessorSpec(environment))
                .addMethod(propSpec.getSetterSpec(environment, builderClassName));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Audit-prop entity generation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private JavaFile generateAuditPropEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final ClassName auditEntityClassName,
            final String auditPropTypePkg,
            final String auditPropTypeName)
    {
        final var auditedEntityTitle = getEntityTitle(auditedType);
        final var auditPropTypeClassName = ClassName.get(auditPropTypePkg, auditPropTypeName);
        final var synAuditTypeName = ClassName.get(auditEntityClassName.packageName(), "Re%s_a3t".formatted(auditedType.getSimpleName()));

        final var builder = classBuilder(auditPropTypeClassName)
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(javaPoet.getClassName(AbstractAuditProp.class), auditEntityClassName))
                .addAnnotation(AnnotationSpecs.entityTitle("%s Audit Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(AnnotationSpecs.keyTitle("%s Audit and Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(javaPoet.getAnnotation(MapEntityTo.class))
                .addAnnotation(AnnotationSpecs.auditPropFor(auditEntityClassName))
                .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class))
                .addAnnotation(javaPoet.getAnnotation(SkipVerification.class))
                .addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class))
                ;

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var auditEntityProp = propertyBuilder(AUDIT_ENTITY, auditEntityClassName)
                .addAnnotation(compositeKeyMember(1))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(AnnotationSpecs.title("%s Audit".formatted(auditedEntityTitle),
                                                     "The audit event associated with this changed property."))
                .build();

        PropertySpec.addProperty(environment, builder, auditPropTypeClassName, auditEntityProp);

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var pdProp = propertyBuilder(PROPERTY, ParameterizedTypeName.get(javaPoet.getClassName(PropertyDescriptor.class), synAuditTypeName))
                .addAnnotation(compositeKeyMember(2))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(AnnotationSpecs.title("Changed Property", "The property that was changed as part of the audit event."))
                .build();
        // This property has a custom setter so has to be added by hand
        builder.addField(pdProp.toFieldSpec(environment));
        builder.addMethod(pdProp.getAccessorSpec(environment));
        // Ideally, PropertyDescriptor in the setter's parameter type would be parameterised with the synthetic audit-entity type
        // to match the property type, but the setter cannot be overriden in such a way, so we use the raw type.
        builder.addMethod(pdProp.getSetterSpecWithParamType(environment, auditPropTypeClassName, javaPoet.getClassName(PropertyDescriptor.class)));

        final var typeSpec = builder.build();

        return JavaFile.builder(auditPropTypePkg, typeSpec).build();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Synthetic audit-entity generation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Override
    public SourceInfo generateSyn(final Class<? extends AbstractEntity<?>> entityType) {
        final var javaFile = generateSyn_(entityType);

        final var fqn = Stream.of(javaFile.packageName, javaFile.typeSpec.name).filter(s -> !s.isEmpty()).collect(joining("."));
        final var source = JavaPoet.readJavaFile(javaFile);
        return new SourceInfo(fqn, source);
    }

    private JavaFile generateSyn_(final Class<? extends AbstractEntity<?>> entityType) {
        validateAuditedType(entityType);

        // non-empty
        final var auditEntityTypes = auditTypeFinder.getAllAuditEntityTypesFor(entityType)
                .stream()
                .sorted(comparing(AuditUtils::getAuditEntityTypeVersion))
                .toList();

        final var synEntitySimpleName = "Re%s_a3t".formatted(entityType.getSimpleName());

        return generateSyn_(entityType, entityType.getPackageName(), synEntitySimpleName, auditEntityTypes.stream().map(this::newAuditEntitySpec).toList());
    }

    @Override
    public SourceInfo generateSyn(final Class<? extends AbstractEntity<?>> entityType, final Path outputPath) {
        final var javaFile = generateSyn_(entityType);
        try {
            javaFile.writeTo(outputPath);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final var fqn = Stream.of(javaFile.packageName, javaFile.typeSpec.name).filter(s -> !s.isEmpty()).collect(joining("."));
        final var source = JavaPoet.readJavaFile(javaFile);
        return new SourceInfo(fqn, source);
    }

    private JavaFile generateSyn_(
            final Class<? extends AbstractEntity<?>> auditedEntityType,
            final String pkgName,
            final String simpleName,
            final List<AuditEntitySpec> auditEntitySpecs)
    {
        if (auditEntitySpecs.isEmpty()) {
            throw new InvalidArgumentException(
                    format("Synthetic audit-entity type for [%s] cannot be generated: there are no audit-entity types.",
                           auditedEntityType.getSimpleName()));
        }

        final var className = ClassName.get(pkgName, simpleName);
        final var builder = classBuilder(className);

        builder.addModifiers(PUBLIC);

        builder.superclass(ParameterizedTypeName.get(AbstractSynAuditEntity.class, auditedEntityType));

        builder.addAnnotation(synAuditFor(auditedEntityType));
        builder.addAnnotation(AnnotationSpecs.entityTitle("%s Audit".formatted(getEntityTitle(auditedEntityType))));
        builder.addAnnotation(javaPoet.getAnnotation(SkipVerification.class));
        builder.addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class));
        builder.addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class));

        // Declare key member "auditedEntity", common to all audit-entity type versions.
        final var auditedEntityProp = propertyBuilder(AbstractSynAuditEntity.AUDITED_ENTITY, auditedEntityType)
                .addAnnotation(compositeKeyMember(AbstractSynAuditEntity.AUDITED_ENTITY_KEY_MEMBER_ORDER))
                .addAnnotation(AnnotationSpecs.title(getEntityTitle(auditedEntityType),
                                                     "The audited %s.".formatted(getEntityTitle(auditedEntityType))))
                .build();
        addPropertyTo(auditedEntityProp, builder, className);

        // "Current" audit-entity type (latest audit-entity type version).
        final var currentAuditEntitySpec = auditEntitySpecs.getLast();
        final var currentAuditPropertySpecs = currentAuditEntitySpec.properties.stream()
                .filter(p -> isAuditProperty(p.name()))
                .toList();

        final var priorAuditEntityTypeSpecs = dropRight(auditEntitySpecs, 1);

        final Function<PropertySpec, String> propertyNameGenerator = new Function<>() {
            final IdentityHashMap<PropertySpec, String> cache = new IdentityHashMap<>();
            long counter = 0;

            public String apply(final PropertySpec property) {
                return cache.computeIfAbsent(property, p -> "$%s_%s".formatted(counter++, p.name()));
            }
        };

        // Sorted by version, from highest to lowest.
        final var sortedAuditEntitySpecs = auditEntitySpecs.stream()
                .sorted(comparing(AuditEntitySpec::version).reversed())
                .collect(toImmutableList());

        // Associates each distinct audit property with the audit-entity that declares it.
        final Map<PropertySpec, ClassName> auditPropertyOriginMap =
                distinct(sortedAuditEntitySpecs.stream()
                                 .flatMap(ts -> ts.properties.stream()
                                         .filter(p -> isAuditProperty(p.name()))
                                         .map(p -> t2(ts, p))),
                         pair -> t2(pair._2.name(), pair._2.type()))
                        .collect(toImmutableMap(pair -> pair._2, pair -> pair._1.className()));

        // Audit properties from all audit-entity type versions and the names they are declared under in the synthetic entity.
        // "Old" audit properties are declared in the synthetic audit-entity using generated names to avoid potential conflicts
        // with current audit properties.
        // Old audit properties are those that are present in a prior audit-entity type and absent in the current one.
        final Map<PropertySpec, String> allAuditProperties =
                auditPropertyOriginMap.keySet()
                        .stream()
                        .collect(toImmutableMap(Function.identity(),
                                                p -> currentAuditEntitySpec.hasProperty(p) ? p.name() : propertyNameGenerator.apply(p)));

        // Declare all audit properties.
        allAuditProperties.forEach((prop, name) -> {
            final var title = prop.title().orElse("");
            addPropertyTo(propertyBuilder(name, prop.type())
                                  .addAnnotation(AnnotationSpecs.title(title, "[%s] at the time of the audited event.".formatted(title)))
                                  .build(),
                          builder, className);
        });

        // EQL models

        final var eqlQueryType = ParameterizedTypeName.get(javaPoet.getClassName(EntityResultQueryModel.class), className);

        final Map<PropertySpec, String> allOldAuditProperties = Maps.filterKeys(allAuditProperties, p -> !currentAuditEntitySpec.hasProperty(p));

        // Yield null into audit properties absent from the current audit-entity type (i.e., old properties).
        final var currentModelField = FieldSpec.builder(eqlQueryType,
                                                        "model_a3t_%s".formatted(currentAuditEntitySpec.version()),
                                                        PRIVATE, STATIC, FINAL)
                .initializer("$T.$L($L.class, $T.class, $L)",
                             SynAuditEntityUtils.class,
                             "mkModelCurrent",
                             className,
                             currentAuditEntitySpec.className(),
                             codeMapOf(mkNullYields(allOldAuditProperties), "$S", "$L"))
                .build();

        final Map<AuditEntitySpec, FieldSpec> priorModelFieldsMap =
                priorAuditEntityTypeSpecs.stream()
                        .collect(toImmutableMap(
                                Function.identity(),
                                priorAuditEntityType -> {
                                    // Yield null into audit properties absent from this prior audit-entity type.
                                    final var nullYieldsArg = codeMapOf(
                                            mkNullYields(Maps.filterKeys(allAuditProperties, p -> !priorAuditEntityType.hasProperty(p))),
                                            "$S", "$L");
                                    // Yield old properties under generated names.
                                    final var renamedYieldsArg = codeMapOf(
                                            map(Maps.filterKeys(allOldAuditProperties, priorAuditEntityType::hasProperty),
                                                               (pm, $) -> pm.name(),
                                                               ($, genName) -> genName),
                                            "$S", "$S");
                                    // Properties present in both the current and prior audit-entity types are yielded as usual.
                                    final var otherYieldsArg = codeSetOf(
                                            concatSet(currentAuditPropertySpecs.stream()
                                                              .filter(priorAuditEntityType::hasProperty)
                                                              .map(PropertySpec::name)
                                                              .collect(toSet()),
                                                      Set.of(AbstractEntity.ID, AbstractEntity.VERSION),
                                                      AbstractSynAuditEntity.BASE_PROPERTIES));

                                    return FieldSpec.builder(eqlQueryType,
                                                             "model_a3t_%s".formatted(priorAuditEntityType.version()),
                                                             PRIVATE, STATIC, FINAL)
                                            .initializer("$T.$L($L.class, $T.class, $L, $L, $L)",
                                                         SynAuditEntityUtils.class,
                                                         "mkModelPrior",
                                                         className,
                                                         priorAuditEntityType.className(),
                                                         nullYieldsArg,
                                                         renamedYieldsArg,
                                                         otherYieldsArg)
                                            .build();
                                }));

        // From highest version to lowest
        final var sortedPriorModelFields = priorModelFieldsMap.entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey, comparing(AuditEntitySpec::version).reversed()))
                .map(Map.Entry::getValue)
                .toList();
        final var modelField = FieldSpec.builder(eqlQueryType, "model_", PROTECTED, STATIC, FINAL)
                .initializer(CodeBlock.of("$T.$L($L)",
                                          SynAuditEntityUtils.class,
                                          "combineModels",
                                          concatList(List.of(currentModelField), sortedPriorModelFields)
                                                  .stream()
                                                  .map(field -> CodeBlock.of("$L", field.name))
                                                  .map(CodeBlock::toString)
                                                  .collect(joining(", "))))
                .build();

        builder.addField(currentModelField);
        builder.addFields(sortedPriorModelFields);
        builder.addField(modelField);

        return JavaFile.builder(pkgName, builder.build()).build();
    }

    /**
     * Creates a map of yields ({@code {alias : value}}), where {@code value} is always {@code null}, and where {@code null}
     * is not applicable, some default value is used.
     * <p>
     * The definition of <i>default value</i> may be refined in the future.
     *
     * @param propertyMap  a map containing properties for which null-yields are to be created;
     *                     values in the map are corresponding names as declared by a synthetic entity
     */
    private static Map<String, Object> mkNullYields(final Map<PropertySpec, String> propertyMap) {
        final var map = new HashMap<String, Object>(propertyMap.size());

        propertyMap.forEach((prop, name) -> {
            final Object value;
            if (prop.type().equals(TypeName.BOOLEAN)) {
                value = false;
            }
            else {
                value = null;
            }
            map.put(name, value);
        });

        return unmodifiableMap(map);
    }

    /**
     * Generates code that calls {@link Set#of(Object[])} with the specified strings as arguments.
     */
    private CodeBlock codeSetOf(final Iterable<String> strings) {
        return CodeBlock.of("$T.of($L)",
                            Set.class,
                            Streams.stream(strings)
                                    .map(s -> CodeBlock.of("$S", s))
                                    .map(CodeBlock::toString)
                                    .collect(joining(", ")));
    }

    /**
     * Generates code that calls {@link CollectionUtil#mapOf(T2[])} with arguments from the specified map (each entry becomes a pair).
     *
     * @param keyFormat  format specifier for keys (see {@link CodeBlock})
     * @param valueFormat  format specifier for values (see {@link CodeBlock})
     */
    private CodeBlock codeMapOf(final Map<?, ?> map, final String keyFormat, final String valueFormat) {
        return CodeBlock.of("$T.mapOf($L)",
                            CollectionUtil.class,
                            map.entrySet().stream()
                                    .map(entry -> CodeBlock.of("$T.t2(%s, %s)".formatted(keyFormat, valueFormat),
                                                               T2.class, entry.getKey(), entry.getValue()))
                                    .map(CodeBlock::toString)
                                    .collect(joining(", ")));
    }

}
