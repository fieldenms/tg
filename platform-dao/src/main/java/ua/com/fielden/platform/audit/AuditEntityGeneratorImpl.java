package ua.com.fielden.platform.audit;

import com.google.common.collect.Streams;
import com.squareup.javapoet.*;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PUBLIC;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.*;
import static ua.com.fielden.platform.audit.AbstractAuditProp.AUDIT_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditProp.PROPERTY;
import static ua.com.fielden.platform.audit.PropertySpec.propertyBuilder;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;

final class AuditEntityGeneratorImpl implements AuditEntityGenerator {

    private final IDomainMetadata domainMetadata;
    private final GeneratorEnvironment environment;
    private final JavaPoet javaPoet;

    @Inject
    AuditEntityGeneratorImpl(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
        environment = new GeneratorEnvironment();
        javaPoet = environment.javaPoet();
    }

    @Override
    public Set<GeneratedResult> generate(
            final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            final Path sourceRoot)
    {
        entityTypes.forEach(AuditEntityGeneratorImpl::validateAuditedType);
        return Streams.stream(entityTypes)
                .parallel()
                .map(type -> {
                    final var result = generate_(type);
                    final Path auditEntityPath;
                    final Path auditPropPath;
                    try {
                        auditEntityPath = result.auditEntity.writeToPath(sourceRoot);
                        auditPropPath = result.auditProp.writeToPath(sourceRoot);
                    } catch (final IOException e) {
                        throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(result.auditVersion, type.getTypeName()), e);
                    }
                    return new GeneratedResult(auditEntityPath, auditPropPath);
                })
                .collect(toImmutableSet());
    }

    @Override
    public Set<SourceInfo> generateSources(final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes) {
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
                    final var result = generate_(type);
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

    private LocalResult generate_(final Class<? extends AbstractEntity<?>> type) {
        final var auditTypePkg = type.getPackageName();
        // TODO multiple audit-entity versions
        final var auditTypeVersion = 1;
        final var auditTypeName = type.getSimpleName() + "_" + A3T + "_" + auditTypeVersion;
        final var auditPropTypeName = auditTypeName + "_Prop";

        final JavaFile auditEntity;
        final JavaFile auditProp;
        try {
            auditEntity = generateAuditEntity(type, auditTypePkg, auditTypeName, ClassName.get(auditTypePkg, auditPropTypeName), auditTypeVersion);
            auditProp = generateAuditPropEntity(type, ClassName.get(auditTypePkg, auditTypeName), auditTypePkg, auditPropTypeName);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(auditTypeVersion, type.getTypeName()), e);
        }

        return new LocalResult(auditEntity, auditProp, auditTypeVersion);
    }

    record LocalResult(JavaFile auditEntity, JavaFile auditProp, int auditVersion) {}

    private JavaFile generateAuditEntity(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName,
            final ClassName auditPropTypeName,
            final int auditTypeVersion)
    {
        final var auditTypeClassName = ClassName.get(auditTypePkg, auditTypeName);

        final var a3tBuilder = new AuditEntityBuilder(auditTypeClassName, type, auditTypeVersion);

        a3tBuilder.addAnnotation(AnnotationSpecs.entityTitle("%s Audit".formatted(TitlesDescsGetter.getEntityTitle(type))));

        // Property for the reference to the audited entity.
        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var auditedEntityTitle = TitlesDescsGetter.getEntityTitle(type);
        final var auditedEntityProp = propertyBuilder(AUDITED_ENTITY, type)
                .addAnnotation(AnnotationSpecs.compositeKeyMember(AbstractAuditEntity.NEXT_COMPOSITE_KEY_MEMBER))
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
                            .addAnnotation(
                                    AnnotationSpecs.mapTo((AuditUtils.auditPropertyName(pm.name())).toUpperCase()))
                            .addAnnotation(javaPoet.getAnnotation(Final.class));

                    final var propTitle = TitlesDescsGetter.getTitleAndDesc(pm.name(), type).getKey();
                    if (!propTitle.isEmpty()) {
                        propBuilder.addAnnotation(AnnotationSpecs.title(propTitle, "[%s] at the time of the audited event.".formatted(propTitle)));
                    }

                    return propBuilder.build();
                })
                .forEach(a3tBuilder::addProperty);

        final var typeSpec = a3tBuilder.build(addSkipEntityExistsValidation);
        return JavaFile.builder(auditTypePkg, typeSpec).build();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : AuditEntityBuilder.Processor declarations
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Annotates each entity-typed property with {@link SkipEntityExistsValidation}, unless this annotation is already present.
     */
    private final AuditEntityBuilder.Processor addSkipEntityExistsValidation = new AuditEntityBuilder.Processor() {
        public PropertySpec processProperty(final AuditEntityBuilder builder, final PropertySpec propSpec) {
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

    private final class AuditEntityBuilder {
        private final ClassName className;
        private final Class<? extends AbstractEntity<?>> auditedType;
        private final ArrayList<PropertySpec> properties;
        private final ArrayList<MethodSpec> methods;
        private final ArrayList<AnnotationSpec> annotations;
        private final int auditVersion;

        private AuditEntityBuilder(
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

        public TypeSpec build(final Processor processor) {
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
            return builder.build();
        }

        public AuditEntityBuilder addProperty(final PropertySpec property) {
            properties.add(property);
            return this;
        }

        public AuditEntityBuilder addProperties(final PropertySpec... properties) {
            for (final var property : properties) {
                this.properties.add(property);
            }
            return this;
        }

        public AuditEntityBuilder addProperties(final Iterable<? extends PropertySpec> properties) {
            properties.forEach(this.properties::add);
            return this;
        }

        public AuditEntityBuilder addMethod(MethodSpec method) {
            methods.add(method);
            return this;
        }

        public AuditEntityBuilder addAnnotation(final AnnotationSpec annotation) {
            annotations.add(annotation);
            return this;
        }

        interface Processor {
            PropertySpec processProperty(AuditEntityBuilder builder, PropertySpec property);
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Audit-prop entity generation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private JavaFile generateAuditPropEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final TypeName auditEntityClassName,
            final String auditPropTypePkg,
            final String auditPropTypeName)
    {
        final var auditedEntityTitle = getEntityTitle(auditedType);
        final var auditPropTypeClassName = ClassName.get(auditPropTypePkg, auditPropTypeName);

        final var builder = classBuilder(auditPropTypeClassName)
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(javaPoet.getClassName(AbstractAuditProp.class), auditEntityClassName))
                .addAnnotation(AnnotationSpecs.entityTitle("%s Audit Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(AnnotationSpecs.keyTitle("%s Audit and Changed Property".formatted(auditedEntityTitle)))
                .addAnnotation(javaPoet.getAnnotation(MapEntityTo.class))
                .addAnnotation(AnnotationSpecs.auditPropFor(auditEntityClassName))
                .addAnnotation(javaPoet.getAnnotation(CompanionIsGenerated.class));

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var auditEntityProp = propertyBuilder(AUDIT_ENTITY, auditEntityClassName)
                .addAnnotation(AnnotationSpecs.compositeKeyMember(1))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(AnnotationSpecs.title("%s Audit".formatted(auditedEntityTitle),
                                                     "The audit event associated with this changed property."))
                .build();

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var pdProp = propertyBuilder(PROPERTY, ParameterizedTypeName.get(
                javaPoet.getClassName(PropertyDescriptor.class), auditEntityClassName))
                .addAnnotation(AnnotationSpecs.compositeKeyMember(2))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(AnnotationSpecs.title("Changed Property", "The property that was changed as part of the audit event."))
                .build();

        PropertySpec.addProperties(environment, builder, auditPropTypeClassName, auditEntityProp, pdProp);

        final var typeSpec = builder.build();

        return JavaFile.builder(auditPropTypePkg, typeSpec).build();
    }

}
