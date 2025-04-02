package ua.com.fielden.platform.audit;

import com.google.common.collect.Streams;
import com.squareup.javapoet.*;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.*;
import static ua.com.fielden.platform.audit.AbstractAuditProp.AUDIT_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditProp.PROPERTY;
import static ua.com.fielden.platform.audit.AnnotationSpecs.*;
import static ua.com.fielden.platform.audit.AuditUtils.*;
import static ua.com.fielden.platform.audit.PropertySpec.propertyBuilder;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.nonBlankPropertyTitle;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.*;

final class AuditEntityGenerator implements IAuditEntityGenerator {

    private final AuditingMode auditingMode;
    private final IDomainMetadata domainMetadata;
    private final GeneratorEnvironment environment;
    private final JavaPoet javaPoet;
    private final IAuditTypeFinder auditTypeFinder;

    @Inject
    AuditEntityGenerator(
            final AuditingMode auditingMode,
            final IDomainMetadata domainMetadata,
            final IAuditTypeFinder auditTypeFinder)
    {
        this.auditingMode = auditingMode;
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
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditEntityGenerator.class, auditingMode);
        }

        entityTypes.forEach(AuditEntityGenerator::validateAuditedType);
        return Streams.stream(entityTypes)
                .parallel()
                .flatMap(type -> {
                    final var result = generate_(type, versionStrategy);
                    final Collection<Path> paths;
                    paths = result.javaFiles.stream().map(jf -> {
                        try {
                            return jf.writeToPath(sourceRoot);
                        } catch (final IOException e) {
                            throw new RuntimeException("Failed to write a generated audit source for audited type [%s]".formatted(type.getTypeName()), e);
                        }
                    }).toList();
                    return paths.stream();
                })
                .collect(toImmutableSet());
    }

    @Override
    public Set<SourceInfo> generateSources(
            final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            final VersionStrategy versionStrategy)
    {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditEntityGenerator.class, auditingMode);
        }

        class $ {
            static SourceInfo makeSourceInfo(final JavaFile javaFile) {
                final var className = javaFile.packageName.isEmpty() ? javaFile.typeSpec.name : javaFile.packageName + '.' + javaFile.typeSpec.name;
                return new SourceInfo(className, javaFile.toString());
            }
        }

        entityTypes.forEach(AuditEntityGenerator::validateAuditedType);
        return Streams.stream(entityTypes)
                .parallel()
                .flatMap(type -> {
                    final var result = generate_(type, versionStrategy);
                    return result.javaFiles.stream().map($::makeSourceInfo);
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
        final var lastAuditTypeVersion = auditTypeFinder.navigate(type)
                .findAuditEntityType()
                .map(AuditUtils::getAuditTypeVersion);
        final var newAuditTypeVersion = switch (versionStrategy) {
            case NEW -> lastAuditTypeVersion.map(v -> v + 1).orElse(1);
            case OVERWRITE_LAST -> lastAuditTypeVersion.orElse(1);
        };

        final var auditTypePkg = type.getPackageName();
        final var auditTypeName = type.getSimpleName() + "_" + A3T + "_" + newAuditTypeVersion;
        final var auditPropTypeName = auditTypeName + "_Prop";

        final AuditEntitySpec auditEntitySpec;
        final JavaFile auditEntityJavaFile;
        final JavaFile auditProp;
        final JavaFile synAuditEntity;
        final JavaFile synAuditPropEntity;
        try {
            final var auditEntitySpec_javaFile = generateAuditEntity(type, auditTypePkg, auditTypeName, newAuditTypeVersion);
            auditEntitySpec = auditEntitySpec_javaFile._1;
            auditEntityJavaFile = auditEntitySpec_javaFile._2;
            auditProp = generateAuditPropEntity(type, auditEntitySpec.className(), auditTypePkg, auditPropTypeName, newAuditTypeVersion);
            synAuditEntity = generateSynAuditEntity(type, auditEntitySpec);
            synAuditPropEntity = generateSynAuditPropEntity(type, newAuditTypeVersion);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(newAuditTypeVersion, type.getTypeName()), e);
        }

        final var genDate = new Date();
        return new LocalResult(Stream.of(auditEntityJavaFile, auditProp, synAuditEntity, synAuditPropEntity)
                                       .map(jf -> embedGenerationDate(jf, genDate))
                                       .toList(),
                               newAuditTypeVersion);
    }

    record LocalResult(Collection<JavaFile> javaFiles, int auditVersion) {}

    private T2<AuditEntitySpec, JavaFile> generateAuditEntity(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName,
            final int auditTypeVersion)
    {
        if (auditTypeVersion == 1) {
            return generateAuditEntity1(type, auditTypePkg, auditTypeName);
        }
        else {
            return generateAuditEntityN(type, auditTypePkg, auditTypeName, auditTypeVersion);
        }
    }

    /**
     * Generates an audit-entity whose version is > 1.
     * <p>
     * The structure is as follows:
     * <ul>
     *   <li> Use the previous audit-entity type version as the superclass.
     *   <li> Declare audit-properties for new audited properties.
     *        An audited property is "new" if it is not audited by the latest audit-entity type.
     *   <li> Declare inactive audit-properties for removed audited properties.
     *        When an audited property is removed, we no longer want to inherit its audit-property, so we must hide it
     *        by declaring it anew as a plain property annotated with {@link InactiveAuditProperty}.
     * </ul>
     *
     * @param type  audited type
     */
    private T2<AuditEntitySpec, JavaFile> generateAuditEntityN(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName,
            final int auditTypeVersion)
    {
        final var prevAuditEntityType = auditTypeFinder.navigate(type).auditEntityType(auditTypeVersion - 1);

        final var auditTypeClassName = ClassName.get(auditTypePkg, auditTypeName);

        final var a3tBuilder = new AuditEntitySpecBuilder(auditTypeClassName, type, auditTypeVersion);

        a3tBuilder.addAnnotation(AnnotationSpecs.entityTitle("%s Audit".formatted(getEntityTitle(type))));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(SkipVerification.class));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class));
        a3tBuilder.addAnnotation(keyType(DynamicEntityKey.class));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class));

        final var activeAuditProperites = domainMetadata.forEntity(prevAuditEntityType)
                .properties()
                .stream()
                .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                .collect(toImmutableSet());
        final var prevAuditEntityMetadata = new AuditEntityMetadata(prevAuditEntityType, activeAuditProperites);

        final var auditedEntityMetadata = domainMetadata.forEntity(type);

        final var newAuditedProperties = auditedEntityMetadata.properties()
                .stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .filter(AuditEntityGenerator::isAudited)
                .filter(not(prevAuditEntityMetadata::containsAuditPropertyFor))
                .collect(toSet());

        // If an audited property was removed, then it must still be among the active audit-properties.
        final var inactiveAuditProperties = prevAuditEntityMetadata.activeAuditProperties()
                .stream()
                .filter(pm -> auditedEntityMetadata.propertyOpt(auditedPropertyName(pm.name())).isEmpty())
                .collect(toSet());

        newAuditedProperties.stream()
                .map(pm -> {
                    final var propBuilder = propertyBuilder(auditPropertyName(pm.name()),
                                                            pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, type, pm.name())))
                            .addAnnotation(AnnotationSpecs.mapTo((auditPropertyName(pm.name())).toUpperCase()))
                            .addAnnotation(javaPoet.getAnnotation(Final.class));

                    final var propTitle = nonBlankPropertyTitle(pm.name(), type);
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(AnnotationSpecs.title(propTitle, "[%s] at the time of the audited event.".formatted(propTitle)));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        inactiveAuditProperties.stream()
                .map(pm -> {
                    final var propBuilder = propertyBuilder(pm.name(), pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, prevAuditEntityMetadata.type(), pm.name())))
                            .addAnnotation(InactiveAuditProperty.class);

                    final var propTitle = nonBlankPropertyTitle(pm.name(), prevAuditEntityMetadata.type());
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(title(propTitle, "Non-existing property."));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        return a3tBuilder.build(addSkipEntityExistsValidation)
                .map2(typeSpec -> JavaFile.builder(auditTypePkg, typeSpec).build());
    }

    record AuditEntityMetadata (Class<? extends AbstractAuditEntity<?>> type,
                                Set<PropertyMetadata> activeAuditProperties)
    {
        boolean containsAuditPropertyFor(final PropertyMetadata auditedProperty) {
            return activeAuditProperties.stream()
                    .anyMatch(auditProp -> auditProp.name().equals(auditPropertyName(auditedProperty.name())) &&
                                           auditProp.type().genericJavaType().equals(auditedProperty.type().genericJavaType()));
        }
    }

    private T2<AuditEntitySpec, JavaFile> generateAuditEntity1(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName)
    {
        final var auditTypeClassName = ClassName.get(auditTypePkg, auditTypeName);

        final var a3tBuilder = new AuditEntitySpecBuilder(auditTypeClassName, type, 1);

        a3tBuilder.addAnnotation(AnnotationSpecs.entityTitle("%s Audit".formatted(getEntityTitle(type))));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(SkipVerification.class));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class));
        a3tBuilder.addAnnotation(keyType(DynamicEntityKey.class));
        a3tBuilder.addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class));

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

        a3tBuilder.addProperty(auditedEntityProp);

        // Audited properties
        final var auditedEntityMetadata = domainMetadata.forEntity(type);
        auditedEntityMetadata.properties().stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .filter(AuditEntityGenerator::isAudited)
                .map(pm -> {
                    final var propBuilder = propertyBuilder(auditPropertyName(pm.name()),
                                                            pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, type, pm.name())))
                            .addAnnotation(
                                    AnnotationSpecs.mapTo((auditPropertyName(pm.name())).toUpperCase()))
                            .addAnnotation(javaPoet.getAnnotation(Final.class));

                    final var propTitle = nonBlankPropertyTitle(pm.name(), type);
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(AnnotationSpecs.title(propTitle, "[%s] at the time of the audited event.".formatted(propTitle)));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        return a3tBuilder.build(addSkipEntityExistsValidation)
                .map2(typeSpec -> JavaFile.builder(auditTypePkg, typeSpec).build());
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
        return !IAuditEntityGenerator.NON_AUDITED_PROPERTIES.contains(property.name());
    }

    /**
     * Combines properties following the rules of Java: declared properties hide inherited properties with the same name.
     * The resulting collection will not contain hidden properties.
     */
    private static List<PropertySpec> mergeProperties(final Collection<PropertySpec> declaredProps, final Collection<PropertySpec> inheritedProps) {
        return StreamUtils.distinct(Stream.concat(declaredProps.stream(), inheritedProps.stream()), PropertySpec::name)
                .toList();
    }

    /**
     * Represents an audit-entity type.
     * This structure unifies runtime (metadata) and source code representations of audit-entities.
     * This becomes necessary when a new audit-entity type is being generated (so there is no metadata for it, as it doesn't yet exist),
     * and we want to use this new audit-entity type and all prior audit-entity types for the generation of a synthetic audit-entity type.
     *
     * @param properties  all properties (declared and inherited)
     */
    private record AuditEntitySpec
            (ClassName className,
             List<PropertySpec> properties,
             int version)
    {

        public boolean hasProperty(final PropertySpec property) {
            return properties.stream().anyMatch(p -> p.name().equals(property.name()) && p.type().equals(property.type()));
        }

        public Optional<PropertySpec> findProperty(final CharSequence name) {
            return properties.stream().filter(p -> p.name().equals(name.toString())).findFirst();
        }

    }

    private static boolean isActiveAuditProperty(final PropertySpec propertySpec) {
        return isAuditProperty(propertySpec.name()) && !propertySpec.hasAnnotation(ClassName.get(InactiveAuditProperty.class));
    }

    private static boolean isInactiveAuditProperty(final PropertySpec propertySpec) {
        return isAuditProperty(propertySpec.name()) && propertySpec.hasAnnotation(ClassName.get(InactiveAuditProperty.class));
    }

    private AuditEntitySpec newAuditEntitySpec(final Class<? extends AbstractAuditEntity<?>> auditType) {
        final var properties = Finder.streamRealProperties(auditType)
                .map(prop -> propertyBuilder(prop.getName(), prop.getGenericType())
                        .addAnnotations(Arrays.stream(prop.getAnnotations()).map(AnnotationSpec::get).toList())
                        // Initializer is not available and also not needed
                        .build())
                .toList();
        return new AuditEntitySpec(ClassName.get(auditType), properties, getAuditTypeVersion(auditType));
    }

    class AuditEntitySpecBuilder {
        final ClassName className;
        final Class<? extends AbstractEntity<?>> auditedType;
        /** Declared properties. */
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

        public T2<AuditEntitySpec, TypeSpec> build(final Processor processor) {
            final var builder = classBuilder(className)
                    .addModifiers(PUBLIC)
                    .superclass(superclassName())
                    .addAnnotation(AnnotationSpecs.auditFor(auditedType, auditVersion))
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

            final var allProperties = auditVersion == 1
                    ? properties
                    : auditTypeFinder.navigate(auditedType).findAuditEntityType(auditVersion - 1)
                            .map(prevAuditType -> mergeProperties(properties, newAuditEntitySpec(prevAuditType).properties()))
                            .orElse(properties);

            return t2(new AuditEntitySpec(className, allProperties, auditVersion), typeSpec);
        }

        public AuditEntitySpecBuilder addProperty(final PropertySpec property) {
            properties.add(property);
            return this;
        }

        private TypeName superclassName() {
            return auditVersion == 1
                    ? ParameterizedTypeName.get(AbstractAuditEntity.class, auditedType)
                    : ClassName.get(className.packageName(), AuditUtils.getAuditTypeName(auditedType.getSimpleName(), auditVersion - 1));
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
            final String auditPropTypeName,
            final int version)
    {
        final var auditedEntityTitle = getEntityTitle(auditedType);
        final var auditPropTypeClassName = ClassName.get(auditPropTypePkg, auditPropTypeName);
        final var synAuditTypeName = ClassName.get(auditEntityClassName.packageName(), "Re%s_a3t".formatted(auditedType.getSimpleName()));

        final var builder = classBuilder(auditPropTypeClassName)
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(AbstractAuditProp.class, auditedType))
                .addAnnotation(AnnotationSpecs.entityTitle("%s Audit Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(AnnotationSpecs.keyTitle("%s Audit and Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(javaPoet.getAnnotation(MapEntityTo.class))
                .addAnnotation(AnnotationSpecs.auditFor(auditedType, version))
                .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class))
                .addAnnotation(javaPoet.getAnnotation(SkipVerification.class))
                .addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class))
                .addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class))
                .addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class))
                .addAnnotation(keyType(DynamicEntityKey.class));

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        // For the setter we need two methods: a regular setter and an override of the setter from the base type,
        // which has a different signature.
        // The override will delegate to the regular setter.
        final var auditEntityProp = propertyBuilder(AUDIT_ENTITY, auditEntityClassName)
                .addAnnotation(compositeKeyMember(1))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(AnnotationSpecs.title("%s Audit".formatted(auditedEntityTitle),
                                                     "The audit event associated with this changed property."))
                .build();

        PropertySpec.addProperty(environment, builder, auditPropTypeClassName, auditEntityProp);

        // Overriden setter.
        final var auditEntityPropSetter = auditEntityProp.getSetterSpec(environment, auditPropTypeClassName);
        builder.addMethod(methodBuilder(auditEntityPropSetter.name)
                                  .addModifiers(PUBLIC)
                                  .returns(auditPropTypeClassName)
                                  .addParameter(ParameterizedTypeName.get(AbstractAuditEntity.class, auditedType),
                                                auditEntityProp.name(),
                                                FINAL)
                                  .addStatement("return $L(($T) $L)",
                                                auditEntityPropSetter.name,
                                                auditEntityClassName,
                                                auditEntityProp.name())
                                  .build());

        // Setter and getter for `property` need to be crafted by hand.
        final var pdProp = propertyBuilder(PROPERTY, ParameterizedTypeName.get(javaPoet.getClassName(PropertyDescriptor.class), synAuditTypeName))
                .addAnnotation(compositeKeyMember(2))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(AnnotationSpecs.title("Changed Property", "The property that was changed as part of the audit event."))
                .build();

        // Avoid clashing with the getter in the base type:
        // * The return type should be parameterised with an abstract type, making it slightly different from the property type.
        // * The returned value must be cast so that it aligns with the return type.
        final var pdGetter = methodBuilder("get" + StringUtils.capitalize(pdProp.name()))
                .addModifiers(PUBLIC)
                .returns(ParameterizedTypeName.get(javaPoet.getClassName(PropertyDescriptor.class),
                                                   ParameterizedTypeName.get(javaPoet.getClassName(AbstractSynAuditEntity.class),
                                                                             javaPoet.getClassName(auditedType))))
                .addStatement("return ($T) this.$L", PropertyDescriptor.class, pdProp.name())
                .build();

        // Use raw PropertyDescriptor as parameter type to avoid clashing with the setter in the base type.
        final var pdSetter = pdProp.getSetterSpecWithParamType(environment, auditPropTypeClassName, javaPoet.getClassName(PropertyDescriptor.class));

        builder.addField(pdProp.toFieldSpec(environment))
                .addMethod(pdGetter)
                .addMethod(pdSetter);

        final var typeSpec = builder.build();

        return JavaFile.builder(auditPropTypePkg, typeSpec).build();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Synthetic audit-entity generation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private JavaFile generateSynAuditEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final AuditEntitySpec auditEntitySpec)
    {
        validateAuditedType(auditedType);

        final var priorAuditEntitySpecs = auditTypeFinder.navigate(auditedType).allAuditEntityTypes()
                .stream()
                .filter(ty -> getAuditTypeVersion(ty) != auditEntitySpec.version())
                .map(this::newAuditEntitySpec)
                .sorted(comparing(AuditEntitySpec::version))
                .toList();

        final var synAuditEntityClassName = ClassName.get(auditedType.getPackageName(), "Re%s_a3t".formatted(auditedType.getSimpleName()));

        return generateSynAuditEntity(auditedType, append(priorAuditEntitySpecs, auditEntitySpec), synAuditEntityClassName);
    }

    /**
     * Generates a synthetic audit-entity.
     * <p>
     * The structure is as follows:
     * <ul>
     *   <li> Declare all audit-properties, both active and inactive, from the latest audit-entity type.
     *   <li> Declare an EQL model for each audit-entity type version, ensuring a rectangular shape.
     * </ul>
     */
    private JavaFile generateSynAuditEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final List<AuditEntitySpec> auditEntitySpecs,
            final ClassName synAuditEntityClassName)
    {
        if (auditEntitySpecs.isEmpty()) {
            throw new InvalidArgumentException(
                    format("Synthetic audit-entity type for [%s] cannot be generated: there are no audit-entity types.",
                           auditedType.getSimpleName()));
        }

        final var builder = classBuilder(synAuditEntityClassName);

        builder.addModifiers(PUBLIC);

        builder.superclass(ParameterizedTypeName.get(AbstractSynAuditEntity.class, auditedType));

        builder.addAnnotation(auditFor(auditedType));
        builder.addAnnotation(AnnotationSpecs.entityTitle("%s Audit".formatted(getEntityTitle(auditedType))));
        builder.addAnnotation(javaPoet.getAnnotation(SkipVerification.class));
        builder.addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class));
        builder.addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class));
        builder.addAnnotation(keyType(DynamicEntityKey.class));

        // Declare key member "auditedEntity", common to all audit-entity type versions.
        final var auditedEntityProp = propertyBuilder(AbstractSynAuditEntity.AUDITED_ENTITY, auditedType)
                .addAnnotation(compositeKeyMember(AbstractSynAuditEntity.AUDITED_ENTITY_KEY_MEMBER_ORDER))
                .addAnnotation(AnnotationSpecs.title(getEntityTitle(auditedType),
                                                     "The audited %s.".formatted(getEntityTitle(auditedType))))
                .build();
        addPropertyTo(auditedEntityProp, builder, synAuditEntityClassName);

        // "changedProps" property
        final var synAuditPropClassName = ClassName.get(synAuditEntityClassName.packageName(), synAuditEntityClassName.simpleName() + "_Prop");
        addPropertyTo(propertyBuilder(AbstractSynAuditEntity.CHANGED_PROPS, ParameterizedTypeName.get(ClassName.get(Set.class), synAuditPropClassName))
                              .addAnnotation(AnnotationSpecs.title("Changed Properties", "Properties changed as part of the audit event."))
                              .initializer("new $T<>()", ClassName.get(HashSet.class))
                              .build(),
                      builder, synAuditEntityClassName);

        // "changedPropsCrit" property
        addPropertyTo(propertyBuilder(AbstractSynAuditEntity.CHANGED_PROPS_CRIT,
                                      ParameterizedTypeName.get(ClassName.get(PropertyDescriptor.class), synAuditEntityClassName))
                              .addAnnotation(AnnotationSpecs.title("Changed Properties", "Properties changed as part of the audit event."))
                              .addAnnotation(AnnotationSpecs.critOnly(CritOnly.Type.MULTI))
                              .build(),
                      builder, synAuditEntityClassName);

        // Sorted by version, from highest to lowest.
        final var sortedAuditEntitySpecs = auditEntitySpecs.stream()
                .sorted(comparing(AuditEntitySpec::version).reversed())
                .collect(toImmutableList());

        final var currAuditEntitySpec = sortedAuditEntitySpecs.getFirst();
        final var activeAuditProperties = currAuditEntitySpec
                .properties()
                .stream()
                .filter(AuditEntityGenerator::isActiveAuditProperty)
                .toList();
        final var inactiveAuditProperties = currAuditEntitySpec
                .properties()
                .stream()
                .filter(AuditEntityGenerator::isInactiveAuditProperty)
                .toList();

        final var allAuditProperties = concatList(activeAuditProperties, inactiveAuditProperties);

        // Declare all audit-properties
        allAuditProperties.stream()
                .map(prop -> {
                    final var title = prop.title().orElse("");
                    final var propBuilder = propertyBuilder(prop.name(), prop.type());
                    propBuilder.addAnnotation(title(title, "[%s] at the time of the audited event.".formatted(title)));
                    if (prop.hasAnnotation(InactiveAuditProperty.class)) {
                        propBuilder.addAnnotation(InactiveAuditProperty.class);
                    }
                    return propBuilder.build();
                })
                .forEach(prop -> addPropertyTo(prop, builder, synAuditEntityClassName));

        final var modelsField = FieldSpec.builder(ParameterizedTypeName.get(javaPoet.getClassName(List.class),
                                                                            ParameterizedTypeName.get(javaPoet.getClassName(EntityResultQueryModel.class),
                                                                                                      synAuditEntityClassName)),
                                                  "models_",
                                                  PROTECTED, STATIC)
                .build();

        builder.addField(modelsField);

        return JavaFile.builder(synAuditEntityClassName.packageName(), builder.build()).build();
    }

    private JavaFile generateSynAuditPropEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final int lastAuditVersion)
    {
        final var auditedEntityTitle = getEntityTitle(auditedType);
        final var synAuditPropClassName = ClassName.get(auditedType.getPackageName(), "Re%s_a3t_Prop".formatted(auditedType.getSimpleName()));
        final var synAuditClassName = ClassName.get(auditedType.getPackageName(), "Re%s_a3t".formatted(auditedType.getSimpleName()));

        final var builder = classBuilder(synAuditPropClassName)
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(AbstractSynAuditProp.class, auditedType))
                .addAnnotation(auditFor(auditedType))
                .addAnnotation(javaPoet.getAnnotation(SkipVerification.class))
                .addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class))
                .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class))
                .addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class))
                .addAnnotation(keyType(DynamicEntityKey.class))
                .addAnnotation(entityTitle("%s Audit Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(AnnotationSpecs.keyTitle("%s Audit and Changed Property".formatted(auditedEntityTitle)));

        // By virtue of its name, this property's accessor implements the abstract method in the base type.
        // For the setter we need two methods: a regular setter and an override of the setter from the base type,
        // which has a different signature.
        // The override will delegate to the regular setter.
        final var auditEntityProp = propertyBuilder(AbstractSynAuditProp.AUDIT_ENTITY, synAuditClassName)
                .addAnnotation(compositeKeyMember(1))
                .addAnnotation(AnnotationSpecs.title("%s Audit".formatted(auditedEntityTitle),
                                                     "The audit event associated with this changed property."))
                .build();
        PropertySpec.addProperty(environment, builder, synAuditPropClassName, auditEntityProp);

        // Overriden setter.
        final var auditEntityPropSetter = auditEntityProp.getSetterSpec(environment, synAuditPropClassName);
        builder.addMethod(methodBuilder(auditEntityPropSetter.name)
                                  .addModifiers(PUBLIC)
                                  .returns(synAuditPropClassName)
                                  .addParameter(ParameterizedTypeName.get(javaPoet.getClassName(AbstractSynAuditEntity.class), javaPoet.getClassName(auditedType)),
                                                auditEntityProp.name(),
                                                FINAL)
                                  .addStatement("return $L(($T) $L)",
                                                auditEntityPropSetter.name,
                                                synAuditClassName,
                                                auditEntityProp.name())
                                  .build());

        // Setter and getter for `property` need to be crafted by hand.
        final var pdProp = propertyBuilder(AbstractSynAuditProp.PROPERTY, ParameterizedTypeName.get(javaPoet.getClassName(PropertyDescriptor.class), synAuditClassName))
                .addAnnotation(compositeKeyMember(2))
                .addAnnotation(AnnotationSpecs.title("Changed Property", "The property that was changed as part of the audit event."))
                .build();

        // Avoid clashing with the getter in the base type:
        // * The return type should be parameterised with an abstract type, making it slightly different from the property type.
        // * The returned value must be cast so that it aligns with the return type.
        final var pdGetter = methodBuilder("get" + StringUtils.capitalize(pdProp.name()))
                .addModifiers(PUBLIC)
                .returns(ParameterizedTypeName.get(javaPoet.getClassName(PropertyDescriptor.class),
                                                   ParameterizedTypeName.get(javaPoet.getClassName(AbstractSynAuditEntity.class),
                                                                             javaPoet.getClassName(auditedType))))
                .addStatement("return ($T) this.$L", PropertyDescriptor.class, pdProp.name())
                .build();

        // Use raw PropertyDescriptor as parameter type to avoid clashing with the setter in the base type.
        final var pdSetter = pdProp.getSetterSpecWithParamType(environment, synAuditPropClassName, javaPoet.getClassName(PropertyDescriptor.class));

        builder.addField(pdProp.toFieldSpec(environment))
                .addMethod(pdGetter)
                .addMethod(pdSetter);

        // Field "models_"
        final var eqlModelTypeName = ParameterizedTypeName.get(ClassName.get(EntityResultQueryModel.class), synAuditPropClassName);
        final var modelsField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), eqlModelTypeName),
                                                  "models_",
                                                  PROTECTED, STATIC)
                .build();

        builder.addField(modelsField);

        return JavaFile.builder(synAuditPropClassName.packageName(), builder.build()).build();
    }


    private static String formatDate(final Date date) {
        class $ {
            static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        }

        return $.format.format(date);
    }

    private static JavaFile embedGenerationDate(final JavaFile javaFile, final Date date) {
        return javaFile.toBuilder()
                .addFileComment("Generation timestamp: %s".formatted(formatDate(date)))
                .build();
    }

}
