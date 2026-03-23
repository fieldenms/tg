package ua.com.fielden.platform.audit;

import com.google.common.collect.Streams;
import com.squareup.javapoet.*;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.annotations.Audited;
import ua.com.fielden.platform.audit.annotations.DisableAuditing;
import ua.com.fielden.platform.audit.annotations.InactiveAuditProperty;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.audit.exceptions.AuditingRuntimeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.*;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_ENTITY_KEY_MEMBER_ORDER;
import static ua.com.fielden.platform.audit.AbstractAuditProp.AUDIT_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditProp.PROPERTY;
import static ua.com.fielden.platform.audit.AnnotationSpecs.*;
import static ua.com.fielden.platform.audit.AuditUtils.*;
import static ua.com.fielden.platform.audit.PropertySpec.propertyBuilder;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.AnnotationReflector.requirePropertyAnnotation;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.nonBlankPropertyTitle;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.append;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

final class AuditEntityGenerator implements IAuditEntityGenerator {

    private static final Logger LOGGER = getLogger();
    public static final String INACTIVE_AUDIT_PROPERTY_TITLE_SUFFIX = " [removed]";
    public static final String TEMPLATE_TITLE_DESC = "[%s] at the time of the audited event.";
    public static final String
            ERR_WRITING_GENERATED_AUDIT_SOURCE = "Failed to write a generated audit source for audited type [%s]",
            ERR_NON_AUDITED_ENTITY_TYPE = "Entity type [%s] cannot be audited. It must be annotated with @%s.",
            ERR_GENERATING_AUDIT_TYPE = "Failed to generate audit types (version: %s) for [%s].",
            ERR_CANNOT_GENERATE_SYNTHETIC_AUDIT_ENTITY = "Synthetic audit-entity type for [%s] cannot be generated: there are no audit-entity types.";

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
                .flatMap(type -> {
                    final var result = generate_(type, versionStrategy);
                    return result.javaFiles.stream().map(jf -> {
                        final Path path;
                        try {
                            path = jf.writeToPath(sourceRoot);
                        } catch (final IOException ex) {
                            throw new AuditingRuntimeException(ERR_WRITING_GENERATED_AUDIT_SOURCE.formatted(type.getTypeName()), ex);
                        }
                        LOGGER.info(() -> "Created an audit source: [%s]".formatted(path));
                        return path;
                    });
                })
                .toList();
    }

    @Override
    public Collection<SourceInfo> generateSources(
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
                .flatMap(type -> {
                    final var result = generate_(type, versionStrategy);
                    return result.javaFiles.stream().map($::makeSourceInfo);
                })
                .toList();
    }

    private static void validateAuditedType(final Class<? extends AbstractEntity<?>> auditedType) {
        if (!AuditUtils.isAudited(auditedType)) {
            throw new AuditingRuntimeException(ERR_NON_AUDITED_ENTITY_TYPE.formatted(auditedType.getTypeName(), Audited.class.getTypeName()));
        }
    }

    private LocalResult generate_(
            final Class<? extends AbstractEntity<?>> auditedType,
            final VersionStrategy versionStrategy)
    {
        final var lastAuditTypeVersion = auditTypeFinder.navigate(auditedType)
                .findAuditEntityType()
                .map(AuditUtils::getAuditTypeVersion);
        final int newAuditTypeVersion = switch (versionStrategy) {
            case NEW -> lastAuditTypeVersion.map(v -> v + 1).orElse(1);
            case OVERWRITE_LAST -> lastAuditTypeVersion.orElse(1);
        };

        final var auditPkg = auditedType.getPackageName();
        final var auditEntitySimpleName = AuditUtils.getAuditTypeName(auditedType.getSimpleName(), newAuditTypeVersion);
        final var auditPropSimpleName = auditEntitySimpleName + "_Prop";

        final AuditEntitySpec auditEntitySpec;
        final JavaFile auditEntityJavaFile;
        final JavaFile auditProp;
        final JavaFile synAuditEntity;
        final JavaFile synAuditPropEntity;
        try {
            final var auditEntitySpec_javaFile = generateAuditEntity(auditedType, auditPkg, auditEntitySimpleName, newAuditTypeVersion);
            auditEntitySpec = auditEntitySpec_javaFile._1;
            auditEntityJavaFile = auditEntitySpec_javaFile._2;
            auditProp = generateAuditPropEntity(auditedType, auditEntitySpec.className(), auditPkg, auditPropSimpleName, newAuditTypeVersion);
            synAuditEntity = generateSynAuditEntity(auditedType, auditPkg, auditEntitySpec);
            synAuditPropEntity = generateSynAuditPropEntity(auditedType, auditPkg);
        } catch (final Exception ex) {
            throw new AuditingRuntimeException(ERR_GENERATING_AUDIT_TYPE.formatted(newAuditTypeVersion, auditedType.getTypeName()), ex);
        }

        final var genDate = new Date();
        return new LocalResult(Stream.of(auditEntityJavaFile, auditProp, synAuditEntity, synAuditPropEntity)
                                       .map(jf -> embedGenerationDate(jf, genDate))
                                       .toList(),
                               newAuditTypeVersion);
    }

    record LocalResult(Collection<JavaFile> javaFiles, int auditVersion) {}

    private T2<AuditEntitySpec, JavaFile> generateAuditEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final String auditPkg,
            final String auditEntitySimpleName,
            final int auditTypeVersion)
    {
        if (auditTypeVersion == 1) {
            return generateAuditEntity1(auditedType, auditPkg, auditEntitySimpleName);
        }
        else {
            return generateAuditEntityN(auditedType, auditPkg, auditEntitySimpleName, auditTypeVersion);
        }
    }

    /// Generates an audit-entity whose version is greater than `1`.
    ///
    /// The structure is as follows:
    /// -  Use the previous audit-entity type version as the superclass.
    /// -  Declare audit-properties for new audited properties.
    ///    An audited property is "new" if it is not audited by the latest audit-entity type.
    /// -  Declare inactive audit-properties for removed audited properties.
    ///    When an audited property is removed, we no longer want to inherit its audit-property, so we must hide it
    ///    by declaring it anew as a plain property annotated with [InactiveAuditProperty].
    ///
    private T2<AuditEntitySpec, JavaFile> generateAuditEntityN(
            final Class<? extends AbstractEntity<?>> auditedType,
            final String auditPkg,
            final String auditEntitySimpleName,
            final int auditTypeVersion)
    {
        final var prevAuditEntityType = auditTypeFinder.navigate(auditedType).auditEntityType(auditTypeVersion - 1);

        final var auditTypeClassName = ClassName.get(auditPkg, auditEntitySimpleName);

        final var a3tBuilder = new AuditEntitySpecBuilder(auditTypeClassName, auditedType, auditTypeVersion);

        final var auditedProperties = domainMetadata.forEntity(auditedType).properties()
                .stream()
                .filter(prop -> isAudited(auditedType, prop.name()))
                .collect(toSet());

        final var activeAuditProperties = domainMetadata.forEntity(prevAuditEntityType)
                .properties()
                .stream()
                .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                .collect(toSet());

        // New audited properties -- auditable properties of the audited type that are not audited by the latest audit-entity type.
        final var newAuditedProperties = auditedProperties.stream()
                .filter(auditedProp -> activeAuditProperties.stream().noneMatch(auditProp -> isAuditPropertyFor(auditProp, auditedProp)))
                .collect(toSet());

        // Find audit-properties whose corresponding properties of the audited type are no longer auditable.
        final var auditPropertiesToDeactivate = activeAuditProperties.stream()
                .filter(auditProp -> auditedProperties.stream().noneMatch(auditedProp -> isAuditPropertyFor(auditProp, auditedProp)))
                .collect(toSet());

        newAuditedProperties.stream()
                .map(pm -> {
                    final var propBuilder = propertyBuilder(auditPropertyName(pm.name()),
                                                            pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, auditedType, pm.name())))
                            .addAnnotation(mkMapToForAudit(auditedType, pm.name()))
                            .addAnnotation(javaPoet.getAnnotation(Final.class));

                    final var propTitle = nonBlankPropertyTitle(pm.name(), auditedType);
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(AnnotationSpecs.title(propTitle, TEMPLATE_TITLE_DESC.formatted(propTitle)));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        auditPropertiesToDeactivate.stream()
                .map(pm -> {
                    final var propBuilder = propertyBuilder(pm.name(), pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, prevAuditEntityType, pm.name())))
                            .addAnnotation(InactiveAuditProperty.class);

                    final var propTitle = nonBlankPropertyTitle(pm.name(), prevAuditEntityType);
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(title(propTitle + INACTIVE_AUDIT_PROPERTY_TITLE_SUFFIX, TEMPLATE_TITLE_DESC.formatted(propTitle)));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        return a3tBuilder.build()
                .map2(typeSpec -> JavaFile.builder(auditPkg, typeSpec).build());
    }

    private AnnotationSpec mkMapToForAudit(final Class<? extends AbstractEntity<?>> auditedType, final String propName) {
        return AnnotationReflector.getPropertyAnnotationOptionally(MapTo.class, auditedType, propName)
                .map(MapTo::value)
                .filter(not(String::isEmpty))
                .map(colName -> AnnotationSpec.builder(MapTo.class)
                        .addMember("value", "$S", "A3T_" + colName)
                        .build())
                .orElseGet(() -> javaPoet.getAnnotation(MapTo.class));
    }

    private static boolean isAuditPropertyFor(final PropertyMetadata auditProp, final PropertyMetadata auditedProp) {
        return auditPropertyName(auditedProp.name()).equals(auditProp.name())
               && auditedProp.type().genericJavaType().equals(auditProp.type().genericJavaType());
    }

    /// Generates the first version of the audit entity.
    ///
    private T2<AuditEntitySpec, JavaFile> generateAuditEntity1(
            final Class<? extends AbstractEntity<?>> auditedType,
            final String auditPkg,
            final String auditEntitySimpleName)
    {
        final var auditEntityClassName = ClassName.get(auditPkg, auditEntitySimpleName);

        final var a3tBuilder = new AuditEntitySpecBuilder(auditEntityClassName, auditedType, 1);

        // Property for the reference to the audited entity.
        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var auditedEntityTitle = getEntityTitle(auditedType);
        final var auditedEntityProp = propertyBuilder(AUDITED_ENTITY, auditedType)
                .addAnnotation(compositeKeyMember(AUDITED_ENTITY_KEY_MEMBER_ORDER))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(javaPoet.getAnnotation(Required.class))
                .addAnnotation(javaPoet.getAnnotation(Final.class))
                .addAnnotation(AnnotationSpecs.title(auditedEntityTitle, "The audited %s.".formatted(auditedEntityTitle)))
                .build();

        a3tBuilder.addProperty(auditedEntityProp);

        // Audited properties
        final var auditedEntityMetadata = domainMetadata.forEntity(auditedType);
        auditedEntityMetadata.properties().stream()
                .filter(prop -> isAudited(auditedType, prop.name()))
                .map(pm -> {
                    final var propBuilder = propertyBuilder(auditPropertyName(pm.name()),
                                                            pm.type().genericJavaType())
                            .addAnnotation(mkIsPropertyForAudit(requirePropertyAnnotation(IsProperty.class, auditedType, pm.name())))
                            .addAnnotation(mkMapToForAudit(auditedType, pm.name()))
                            .addAnnotation(javaPoet.getAnnotation(Final.class));

                    final var propTitle = nonBlankPropertyTitle(pm.name(), auditedType);
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(AnnotationSpecs.title(propTitle, TEMPLATE_TITLE_DESC.formatted(propTitle)));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        return a3tBuilder.build()
                .map2(typeSpec -> JavaFile.builder(auditPkg, typeSpec).build());
    }

    private boolean isAudited(final Class<? extends AbstractEntity<?>> auditedType, final String propName) {
        final var pm = domainMetadata.forProperty(auditedType, propName);
        return !IAuditEntityGenerator.NON_AUDITED_PROPERTIES.contains(propName)
               && pm.isPersistent()
               && !isPropertyAnnotationPresent(DisableAuditing.class, auditedType, propName);
    }

    /// Builds an [IsProperty] annotation for an audit-property from the specified annotation for a corresponding audited property.
    ///
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
            final var length = isProperty.length() == IsProperty.MAX_LENGTH
                    ? CodeBlock.of("$T.MAX_LENGTH", IsProperty.class)
                    : isProperty.length();
            builder.addMember("length", "$L", length);
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

    /// Combines properties following the rules of Java: declared properties hide inherited properties with the same name.
    /// The resulting collection will not contain hidden properties.
    ///
    private static List<PropertySpec> mergeProperties(final Collection<PropertySpec> declaredProps, final Collection<PropertySpec> inheritedProps) {
        return StreamUtils.distinct(Stream.concat(declaredProps.stream(), inheritedProps.stream()), PropertySpec::name)
                .toList();
    }

    /// Represents an audit-entity type.
    /// This structure unifies runtime (metadata) and source code representations of audit-entities.
    /// This becomes necessary when a new audit-entity type is being generated (so there is no metadata for it, as it doesn't yet exist),
    /// and we want to use this new audit-entity type and all prior audit-entity types for the generation of a synthetic audit-entity type.
    ///
    /// @param properties  all properties (declared and inherited)
    ///
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
        return isAuditProperty(propertySpec.name()) && !propertySpec.hasAnnotation(InactiveAuditProperty.class);
    }

    private static boolean isInactiveAuditProperty(final PropertySpec propertySpec) {
        return isAuditProperty(propertySpec.name()) && propertySpec.hasAnnotation(InactiveAuditProperty.class);
    }

    private AuditEntitySpec newAuditEntitySpec(final Class<? extends AbstractAuditEntity<?>> auditType) {
        final var properties = domainMetadata.forEntity(auditType)
                .properties()
                .stream()
                .map(p -> propertyBuilder(p.name(), p.type().genericJavaType())
                        .addAnnotations(AnnotationReflector.getFieldAnnotations(auditType, p.name()).values().stream().map(AnnotationSpec::get).toList())
                        // Initializer is not available and also not needed
                        .build())
                .toList();
        return new AuditEntitySpec(ClassName.get(auditType), properties, getAuditTypeVersion(auditType));
    }

    class AuditEntitySpecBuilder {
        final ClassName className;
        final Class<? extends AbstractEntity<?>> auditedType;
        /// Declared properties.
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

        public T2<AuditEntitySpec, TypeSpec> build() {
            final var builder = classBuilder(className)
                    .addModifiers(PUBLIC)
                    .superclass(superclassName())
                    .addAnnotation(AnnotationSpecs.auditFor(auditedType, auditVersion))
                    .addAnnotation(MapEntityTo.class)
                    .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class))
                    .addAnnotation(AnnotationSpecs.entityTitle("%s Audit %s".formatted(getEntityTitle(auditedType), auditVersion)))
                    .addAnnotation(javaPoet.getAnnotation(SkipVerification.class))
                    .addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class))
                    .addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class))
                    .addAnnotation(keyType(DynamicEntityKey.class))
                    .addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class))
                    .addAnnotations(annotations);
            properties.stream()
                    .sorted(comparing(PropertySpec::name))
                    .forEach(propSpec -> {
                        builder.addField(propSpec.toFieldSpec(environment));
                        builder.addMethod(propSpec.getAccessorSpec(environment));
                        builder.addMethod(propSpec.getSetterSpec(environment, className));
                    });
            builder.addMethods(methods);
            final var typeSpec = builder.build();

            final List<PropertySpec> allProperties;
            if (auditVersion == 1) {
                allProperties = properties;
            }
            else {
                final var prevAuditType = auditTypeFinder.navigate(auditedType).auditEntityType(auditVersion - 1);
                allProperties = mergeProperties(properties, newAuditEntitySpec(prevAuditType).properties());
            }

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
            Collections.addAll(this.properties, properties);
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
            final String auditPkg,
            final String auditPropSimpleName,
            final int version)
    {
        final var auditedEntityTitle = getEntityTitle(auditedType);
        final var auditPropClassName = ClassName.get(auditPkg, auditPropSimpleName);
        final var synAuditEntityClassName = ClassName.get(auditPkg, synAuditEntitySimpleName(auditedType.getSimpleName()));

        final var builder = classBuilder(auditPropClassName)
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(AbstractAuditProp.class, auditedType))
                .addAnnotation(AnnotationSpecs.entityTitle("%s Audit %s Changed Property".formatted(auditedEntityTitle, version)))
                .addAnnotation(AnnotationSpecs.keyTitle("%s Audit and Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(javaPoet.getAnnotation(MapEntityTo.class))
                .addAnnotation(AnnotationSpecs.auditFor(auditedType, version))
                .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class))
                .addAnnotation(javaPoet.getAnnotation(SkipVerification.class))
                .addAnnotation(javaPoet.getAnnotation(SkipEntityRegistration.class))
                .addAnnotation(javaPoet.getAnnotation(WithoutMetaModel.class))
                .addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class))
                .addAnnotation(keyType(DynamicEntityKey.class));

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type.
        // For the setter we need two methods: a regular setter and an override of the setter from the base type, which has a different signature.
        // The override will delegate to the regular setter.
        final var auditEntityProp = propertyBuilder(AUDIT_ENTITY, auditEntityClassName)
                .addAnnotation(compositeKeyMember(1))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(AnnotationSpecs.title("%s Audit".formatted(auditedEntityTitle),
                                                     "The audit event associated with this changed property."))
                .build();

        PropertySpec.addProperty(environment, builder, auditPropClassName, auditEntityProp);

        // Now the overridden setter.
        final var auditEntityPropSetter = auditEntityProp.getSetterSpec(environment, auditPropClassName);
        builder.addMethod(methodBuilder(auditEntityPropSetter.name)
                                  .addModifiers(PUBLIC)
                                  .returns(auditPropClassName)
                                  .addParameter(ParameterizedTypeName.get(AbstractAuditEntity.class, auditedType),
                                                auditEntityProp.name(),
                                                FINAL)
                                  .addStatement("return $L(($T) $L)",
                                                auditEntityPropSetter.name,
                                                auditEntityClassName,
                                                auditEntityProp.name())
                                  .build());

        // Setter and getter for `property` need to be crafted by hand.
        final var pdProp = propertyBuilder(PROPERTY, ParameterizedTypeName.get(javaPoet.getClassName(PropertyDescriptor.class), synAuditEntityClassName))
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
        final var pdSetter = pdProp.getSetterSpecWithParamType(environment, auditPropClassName, javaPoet.getClassName(PropertyDescriptor.class));

        builder.addField(pdProp.toFieldSpec(environment))
                .addMethod(pdGetter)
                .addMethod(pdSetter);

        final var typeSpec = builder.build();

        return JavaFile.builder(auditPkg, typeSpec).build();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Synthetic audit-entity generation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /// @param auditEntitySpec  the latest audit-entity type
    ///
    private JavaFile generateSynAuditEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final String auditPkg,
            final AuditEntitySpec auditEntitySpec)
    {
        validateAuditedType(auditedType);

        final var priorAuditEntitySpecs = auditTypeFinder.navigate(auditedType).allAuditEntityTypes()
                .stream()
                .filter(ty -> getAuditTypeVersion(ty) != auditEntitySpec.version())
                .map(this::newAuditEntitySpec)
                .sorted(comparing(AuditEntitySpec::version))
                .toList();

        final var synAuditEntityClassName = ClassName.get(auditPkg, synAuditEntitySimpleName(auditedType.getSimpleName()));
        return generateSynAuditEntity(auditedType, append(priorAuditEntitySpecs, auditEntitySpec), synAuditEntityClassName);
    }

    /// Generates a synthetic audit-entity.
    ///
    /// The structure is as follows:
    /// * Declare all audit-properties, both active and inactive, from the latest audit-entity type.
    ///
    /// @param auditEntitySpecs  all persistent audit-entity types
    ///
    private JavaFile generateSynAuditEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final List<AuditEntitySpec> auditEntitySpecs,
            final ClassName synAuditEntityClassName)
    {
        if (auditEntitySpecs.isEmpty()) {
            throw new AuditingRuntimeException(ERR_CANNOT_GENERATE_SYNTHETIC_AUDIT_ENTITY.formatted(auditedType.getSimpleName()));
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
                .addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class))
                .build();
        addPropertyTo(auditedEntityProp, builder, synAuditEntityClassName);

        // "changedProps" property
        final var synAuditPropClassName = ClassName.get(synAuditEntityClassName.packageName(), synAuditEntityClassName.simpleName() + "_Prop");
        addPropertyTo(propertyBuilder(AbstractSynAuditEntity.CHANGED_PROPS, ParameterizedTypeName.get(ClassName.get(Set.class), synAuditPropClassName))
                              .addAnnotation(AnnotationSpecs.title("Changed Properties", "Properties changed as part of the audited event."))
                              .addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class))
                              .initializer("new $T<>()", ClassName.get(HashSet.class))
                              .build(),
                      builder, synAuditEntityClassName);

        // "changedPropsCrit" property
        addPropertyTo(propertyBuilder(AbstractSynAuditEntity.CHANGED_PROPS_CRIT,
                                      ParameterizedTypeName.get(ClassName.get(PropertyDescriptor.class), synAuditEntityClassName))
                              .addAnnotation(AnnotationSpecs.title("Changed Properties", "Properties changed as part of the audited event."))
                              .addAnnotation(AnnotationSpecs.critOnly(CritOnly.Type.MULTI))
                              .addAnnotation(javaPoet.getAnnotation(DenyIntrospection.class))
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
                    final var origTitle = prop.hasAnnotation(InactiveAuditProperty.class)
                            ? substringBeforeLast(title, INACTIVE_AUDIT_PROPERTY_TITLE_SUFFIX)
                            : title;
                    final var propBuilder = propertyBuilder(prop.name(), prop.type());
                    propBuilder.addAnnotation(title(title, TEMPLATE_TITLE_DESC.formatted(origTitle)));
                    if (prop.hasAnnotation(InactiveAuditProperty.class)) {
                        propBuilder.addAnnotation(InactiveAuditProperty.class);
                    }
                    return propBuilder.build();
                })
                .sorted(comparing(PropertySpec::name))
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

    private JavaFile generateSynAuditPropEntity(final Class<? extends AbstractEntity<?>> auditedType, final String auditPkg) {
        final var auditedEntityTitle = getEntityTitle(auditedType);
        final var synAuditPropClassName = ClassName.get(auditPkg, synAuditPropSimpleName(auditedType.getSimpleName()));
        final var synAuditClassName = ClassName.get(auditPkg, synAuditEntitySimpleName(auditedType.getSimpleName()));

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
        // For the setter we need two methods: a regular setter and an override of the setter from the base type, which has a different signature.
        // The override will delegate to the regular setter.
        final var auditEntityProp = propertyBuilder(AbstractSynAuditProp.AUDIT_ENTITY, synAuditClassName)
                .addAnnotation(compositeKeyMember(1))
                .addAnnotation(AnnotationSpecs.title("%s Audit".formatted(auditedEntityTitle),
                                                     "The audit event associated with this changed property."))
                .build();
        PropertySpec.addProperty(environment, builder, synAuditPropClassName, auditEntityProp);

        // The overridden setter.
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

    private static String synAuditPropSimpleName(final String auditedTypeSimpleName) {
        return "Re%s_a3t_Prop".formatted(auditedTypeSimpleName);
    }

    private static String synAuditEntitySimpleName(final CharSequence auditedTypeSimpleName) {
        return "Re%s_a3t".formatted(auditedTypeSimpleName);
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
