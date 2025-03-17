package ua.com.fielden.platform.audit;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.squareup.javapoet.*;
import jakarta.inject.Inject;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
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
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.StreamUtils;

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
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;
import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.*;
import static ua.com.fielden.platform.audit.AbstractAuditProp.AUDIT_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditProp.PROPERTY;
import static ua.com.fielden.platform.audit.AnnotationSpecs.*;
import static ua.com.fielden.platform.audit.AuditUtils.*;
import static ua.com.fielden.platform.audit.JavaPoet.topLevelClassName;
import static ua.com.fielden.platform.audit.PropertySpec.propertyBuilder;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.nonBlankPropertyTitle;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.*;

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
        final var lastAuditTypeVersion = auditTypeFinder.findAuditEntityType(type).map(AuditUtils::getAuditEntityTypeVersion);
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
            auditProp = generateAuditPropEntity(type, auditEntitySpec.className(), auditTypePkg, auditPropTypeName);
            synAuditEntity = generateSynAuditEntity(type, auditEntitySpec);
            synAuditPropEntity = generateSynAuditPropEntity(type, newAuditTypeVersion);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(newAuditTypeVersion, type.getTypeName()), e);
        }

        return new LocalResult(List.of(auditEntityJavaFile, auditProp, synAuditEntity, synAuditPropEntity),
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
     *   <li> Declare hidden audit-properties for removed audited properties.
     *        When an audited property is removed, we no longer want to inherit its audit-property, so we must hide it
     *        by declaring it anew as a plain property.
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
        final var prevAuditEntityType = auditTypeFinder.getAuditEntityType(type, auditTypeVersion - 1);

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
                .filter(AuditEntityGeneratorImpl::isAudited)
                .filter(not(prevAuditEntityMetadata::containsAuditPropertyFor))
                .collect(toSet());

        // If an audited property was removed, then it must still be among the active audit-properties.
        final var hiddenAuditProperties = prevAuditEntityMetadata.activeAuditProperties()
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

        hiddenAuditProperties.stream()
                .map(pm -> {
                    final var propBuilder = propertyBuilder(pm.name(), pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, prevAuditEntityMetadata.type(), pm.name())));

                    final var propTitle = nonBlankPropertyTitle(pm.name(), prevAuditEntityMetadata.type());
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(AnnotationSpecs.title(propTitle, "Non-existing property."));
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
                .filter(AuditEntityGeneratorImpl::isAudited)
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
        return !AuditEntityGenerator.NON_AUDITED_PROPERTIES.contains(property.name());
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
        return isAuditProperty(propertySpec.name()) && propertySpec.hasAnnotation(ClassName.get(MapTo.class));
    }

    private static boolean isHiddenAuditProperty(final PropertySpec propertySpec) {
        return isAuditProperty(propertySpec.name()) && !propertySpec.hasAnnotation(ClassName.get(MapTo.class));
    }

    private AuditEntitySpec newAuditEntitySpec(final Class<? extends AbstractAuditEntity<?>> auditType) {
        final var properties = Finder.streamRealProperties(auditType)
                .map(prop -> propertyBuilder(prop.getName(), prop.getGenericType())
                        .addAnnotations(Arrays.stream(prop.getAnnotations()).map(AnnotationSpec::get).toList())
                        // Initializer is not available and also not needed
                        .build())
                .toList();
        return new AuditEntitySpec(ClassName.get(auditType), properties, getAuditEntityTypeVersion(auditType));
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
                    : auditTypeFinder.findAuditEntityType(auditedType, auditVersion - 1)
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
                .addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class))
                .addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class))
                .addAnnotation(keyType(DynamicEntityKey.class));

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

    private JavaFile generateSynAuditEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final AuditEntitySpec auditEntitySpec)
    {
        validateAuditedType(auditedType);

        final var priorAuditEntitySpecs = auditTypeFinder.findAllAuditEntityTypesFor(auditedType)
                .stream()
                .filter(ty -> getAuditEntityTypeVersion(ty) != auditEntitySpec.version())
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
     *   <li> Declare all audit-properties, both active and hidden, from the latest audit-entity type.
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

        builder.addAnnotation(synAuditFor(auditedType));
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

        final var priorAuditEntityTypeSpecs = dropRight(auditEntitySpecs, 1);

        // Sorted by version, from highest to lowest.
        final var sortedAuditEntitySpecs = auditEntitySpecs.stream()
                .sorted(comparing(AuditEntitySpec::version).reversed())
                .collect(toImmutableList());

        final var currAuditEntitySpec = sortedAuditEntitySpecs.getFirst();
        final var activeAuditProperties = currAuditEntitySpec
                .properties()
                .stream()
                .filter(AuditEntityGeneratorImpl::isActiveAuditProperty)
                .toList();
        final var hiddenAuditProperties = currAuditEntitySpec
                .properties()
                .stream()
                .filter(AuditEntityGeneratorImpl::isHiddenAuditProperty)
                .toList();

        final var allAuditProperties = concatList(activeAuditProperties, hiddenAuditProperties);

        // Declare all audit-properties
        allAuditProperties.stream()
                .map(prop -> {
                    final var title = prop.title().orElse("");
                    return propertyBuilder(prop.name(), prop.type())
                            .addAnnotation(AnnotationSpecs.title(title, "[%s] at the time of the audited event.".formatted(title)))
                            .build();
                })
                .forEach(prop -> addPropertyTo(prop, builder, synAuditEntityClassName));

        // EQL models

        final var eqlQueryType = ParameterizedTypeName.get(javaPoet.getClassName(EntityResultQueryModel.class), synAuditEntityClassName);

        final var currentModelField = FieldSpec.builder(eqlQueryType,
                                                        "model_a3t_%s".formatted(currAuditEntitySpec.version()),
                                                        PRIVATE, STATIC, FINAL)
                .initializer("$T.$L($L.class, $T.class, $L, $L)",
                             SynAuditEntityUtils.class,
                             "mkModelCurrent",
                             synAuditEntityClassName,
                             currAuditEntitySpec.className(),
                             // Workaround: explicitly yield all audit and service properties, because yieldAll must not
                             // be used when there is only one audit-entity version (EQL will not compile).
                             codeSetOf(concatSet(activeAuditProperties.stream().map(PropertySpec::name).toList(),
                                                 Set.of(ID),
                                                 AbstractSynAuditEntity.BASE_PROPERTIES)),
                             // Yield null into hidden audit-properties
                             codeMapOf(mkNullYields(hiddenAuditProperties), "$S", "$L"))
                .build();

        // Here we create an EQL model for each prior audit-entity type.
        // We have to yield into all audit-properties of the synthetic model.
        // For some prior-audit entity E, each yield is determined by:
        // 1. Status of audit-property in the current audit-entity type.
        // 2. Status of audit-property in E.
        // Status of an audit-property is one of:
        // * Active.
        // * Hidden or absent.
        // Active audit-properties are yielded as is.
        // For hidden or absent ones, "nothing" is yielded (null for entity-typed properties, some default value for other types).
        final Map<AuditEntitySpec, FieldSpec> priorModelFieldsMap =
                priorAuditEntityTypeSpecs.stream()
                        .collect(toImmutableMap(
                                Function.identity(),
                                priorAuditEntityType -> {
                                    // Key: audit-property in the current audit-entity.
                                    // Value: yielded value -- property name or nothing
                                    final Map<PropertySpec, Optional<String>> yields = allAuditProperties.stream()
                                            .collect(toImmutableMap(
                                                    Function.identity(),
                                                    currAuditProp -> {
                                                        final var propName = currAuditProp.name();
                                                        final var priorAuditProp = priorAuditEntityType.findProperty(propName);
                                                        final var priorAuditPropActive = priorAuditProp.filter(AuditEntityGeneratorImpl::isActiveAuditProperty).isPresent();
                                                        final var currAuditPropActive = isActiveAuditProperty(currAuditProp);
                                                        if (currAuditPropActive && priorAuditPropActive) {
                                                            return Optional.of(propName);
                                                        }
                                                        else if (currAuditPropActive && !priorAuditPropActive) {
                                                            return Optional.empty();
                                                        }
                                                        else if (!currAuditPropActive && priorAuditPropActive) {
                                                            return Optional.of(propName);
                                                        }
                                                        else /* if (!currAuditPropActive && !priorAuditPropActive) */ {
                                                            return Optional.empty();
                                                        }
                                                    }
                                            ));

                                    final var nullYieldsArg = codeMapOf(
                                            mkNullYields(Maps.filterValues(yields, Optional::isEmpty).keySet()),
                                            "$S", "$L");

                                    final var otherYieldsArg = codeSetOf(
                                            concatSet(Maps.filterValues(yields, Optional::isPresent).keySet()
                                                              .stream().map(PropertySpec::name).toList(),
                                                      Set.of(AbstractEntity.ID),
                                                      AbstractSynAuditEntity.BASE_PROPERTIES));

                                    return FieldSpec.builder(eqlQueryType,
                                                             "model_a3t_%s".formatted(priorAuditEntityType.version()),
                                                             PRIVATE, STATIC, FINAL)
                                            .initializer("$T.$L($L.class, $T.class, $L, $L)",
                                                         SynAuditEntityUtils.class,
                                                         "mkModelPrior",
                                                         synAuditEntityClassName,
                                                         priorAuditEntityType.className(),
                                                         nullYieldsArg,
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
                .initializer(CodeBlock.of("$T.$L($T.class, $L)",
                                          SynAuditEntityUtils.class,
                                          "combineModels",
                                          synAuditPropClassName,
                                          concatList(List.of(currentModelField), sortedPriorModelFields)
                                                  .stream()
                                                  .map(field -> CodeBlock.of("$L", field.name))
                                                  .map(CodeBlock::toString)
                                                  .collect(joining(", "))))
                .build();

        builder.addField(currentModelField);
        builder.addFields(sortedPriorModelFields);
        builder.addField(modelField);

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
                .superclass(ParameterizedTypeName.get(ClassName.get(AbstractSynAuditProp.class), synAuditClassName))
                .addAnnotation(javaPoet.getAnnotation(SkipVerification.class))
                .addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class))
                .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class))
                .addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class))
                .addAnnotation(keyType(DynamicEntityKey.class))
                .addAnnotation(entityTitle("%s Audit Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(AnnotationSpecs.keyTitle("%s Audit and Changed Property".formatted(auditedEntityTitle)));

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var auditEntityProp = propertyBuilder(AbstractSynAuditProp.AUDIT_ENTITY, synAuditClassName)
                .addAnnotation(compositeKeyMember(1))
                .addAnnotation(AnnotationSpecs.title("%s Audit".formatted(auditedEntityTitle),
                                                     "The audit event associated with this changed property."))
                .build();
        PropertySpec.addProperty(environment, builder, synAuditPropClassName, auditEntityProp);

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var pdProp = propertyBuilder(AbstractSynAuditProp.PROPERTY, ParameterizedTypeName.get(javaPoet.getClassName(PropertyDescriptor.class), synAuditClassName))
                .addAnnotation(compositeKeyMember(2))
                .addAnnotation(AnnotationSpecs.title("Changed Property", "The property that was changed as part of the audit event."))
                .build();
        PropertySpec.addProperty(environment, builder, synAuditPropClassName, pdProp);

        // EQL models, one for each audit-prop version
        final var eqlModelTypeName = ParameterizedTypeName.get(ClassName.get(EntityResultQueryModel.class), synAuditPropClassName);
        final var eqlModelFieldSpecs = rangeClosed(1, lastAuditVersion)
                .mapToObj(i -> {
                    final var auditClassName = topLevelClassName(getAuditTypeName(auditedType, i));
                    final var auditPropClassName = topLevelClassName(getAuditTypeName(auditedType, i) + "_Prop");
                    return FieldSpec.builder(eqlModelTypeName, "model_a3t_%s".formatted(i), PRIVATE, STATIC, FINAL)
                            .initializer(CodeBlock.of("$T.$L($T.class, $T.class, $T.class, $T.class)",
                                                      SynAuditPropEntityUtils.class,
                                                      "modelAuditProp",
                                                      auditPropClassName,
                                                      synAuditPropClassName,
                                                      auditClassName,
                                                      synAuditClassName))
                            .build();
                })
                .toList();

        builder.addFields(eqlModelFieldSpecs);

        // Field "models_"
        final var modelsFieldSpec = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), eqlModelTypeName), "models_", PROTECTED, STATIC, FINAL)
                .initializer("$T.of($L)", List.class, eqlModelFieldSpecs.stream().map(f -> f.name).collect(joining(", ")))
                .build();

        builder.addField(modelsFieldSpec);

        return JavaFile.builder(synAuditPropClassName.packageName(), builder.build()).build();
    }


    /**
     * Creates a map of yields ({@code {alias : value}}), where {@code value} is always {@code null}, and where {@code null}
     * is not applicable, some default value is used.
     * <p>
     * The definition of <i>default value</i> may be refined in the future.
     *
     * @param properties  properties for which null-yields are to be created;
     */
    private static Map<String, Object> mkNullYields(final Collection<PropertySpec> properties) {
        final var map = new HashMap<String, Object>(properties.size());

        properties.forEach(prop -> {
            final Object value;
            if (prop.type().equals(TypeName.BOOLEAN)) {
                value = false;
            }
            else {
                value = null;
            }
            map.put(prop.name(), value);
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
