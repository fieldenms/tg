package ua.com.fielden.platform.audit;

import com.google.common.collect.Streams;
import com.squareup.javapoet.*;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.A3T;
import static ua.com.fielden.platform.audit.PropertySpec.propertyBuilder;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

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
                .map(type -> generate_(type, sourceRoot))
                .collect(toImmutableSet());
    }

    private static void validateAuditedType(final Class<? extends AbstractEntity<?>> auditedType) {
        if (!AuditUtils.isAudited(auditedType)) {
            throw new InvalidArgumentException(format("Entity type [%s] cannot be audited. It must be annotated with @%s.",
                                                      auditedType.getTypeName(), Audited.class.getTypeName()));
        }
    }

    private GeneratedResult generate_(final Class<? extends AbstractEntity<?>> type, final Path sourceRoot) {
        final var auditTypePkg = type.getPackageName();
        // TODO multiple audit-entity versions
        final var auditTypeVersion = 1;
        final var auditTypeName = type.getSimpleName() + "_" + A3T + "_" + auditTypeVersion;
        final var auditTypePath = sourceRoot.resolve(classNameToFilePath(auditTypePkg, auditTypeName));
        final var auditPropTypeName = auditTypeName + "_Prop";
        final var auditPropTypePath = sourceRoot.resolve(classNameToFilePath(auditTypePkg, auditPropTypeName));

        try {
            generateAuditEntity(type, auditTypePkg, auditTypeName, ClassName.get(auditTypePkg, auditPropTypeName), sourceRoot);
            generateAuditPropEntity(type, ClassName.get(auditTypePkg, auditTypeName), auditTypePkg, auditPropTypeName, sourceRoot);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(auditTypeVersion, type.getTypeName()), e);
        }

        return new GeneratedResult(auditTypePath, auditPropTypePath);
    }

    private void generateAuditEntity(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName,
            final ClassName auditPropTypeName,
            final Path sourceRoot)
        throws IOException
    {
        final var auditTypeClassName = ClassName.get(auditTypePkg, auditTypeName);

        final var a3tBuilder = new AuditEntityBuilder(auditTypeClassName, type);

        // Property for the reference to the audited entity
        final var auditedEntityProp = propertyBuilder(uncapitalize(type.getSimpleName()), type)
                .addAnnotation(AnnotationSpecs.compositeKeyMember(AbstractAuditEntity.NEXT_COMPOSITE_KEY_MEMBER))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .addAnnotation(javaPoet.getAnnotation(Required.class))
                .addAnnotation(javaPoet.getAnnotation(Final.class))
                .addAnnotation(AnnotationSpecs.title(getEntityTitleAndDesc(type)))
                .build();

        // Collectional property to model one-to-many association with the audit-prop entity
        final var changedPropsProp = propertyBuilder("changedProps", ParameterizedTypeName.get(javaPoet.getClassName(Set.class), auditPropTypeName))
                .addAnnotation(AnnotationSpecs.title("Changed Properties", "Properties changed as part of an audit event."))
                .initializer("new $T<>()", javaPoet.getClassName(HashSet.class))
                .build();

        a3tBuilder.addProperty(auditedEntityProp);
        a3tBuilder.addProperty(changedPropsProp);

        // Abstract methods in the base audit entity type
        a3tBuilder.addMethod(methodBuilder("getAuditedEntity")
                                     .addModifiers(PUBLIC)
                                     .returns(javaPoet.getClassName(type))
                                     .addAnnotation(javaPoet.getAnnotation(Override.class))
                                     .addStatement("return %s()".formatted(auditedEntityProp.getAccessorSpec(environment).name))
                                     .build());
        a3tBuilder.addMethod(methodBuilder("setAuditedEntity")
                                     .addModifiers(PUBLIC)
                                     .addParameter(javaPoet.getClassName(type), "entity", FINAL)
                                     .returns(auditTypeClassName)
                                     .addAnnotation(javaPoet.getAnnotation(Override.class))
                                     .addStatement("return %s(%s)".formatted(auditedEntityProp.getSetterSpec(environment, auditTypeClassName).name, "entity"))
                                     .build());

        // Audited properties
        final var auditedEntityMetadata = domainMetadata.forEntity(type);
        auditedEntityMetadata.properties().stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .filter(AuditEntityGeneratorImpl::isAudited)
                .map(pm -> propertyBuilder("a3t_" + pm.name(), pm.type().javaType())
                        .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                        .addAnnotation(javaPoet.getAnnotation(Final.class))
                        .build())
                .forEach(a3tBuilder::addProperty);

        final var typeSpec = a3tBuilder.build(addSkipEntityExistsValidation);
        final var javaFile = JavaFile.builder(auditTypePkg, typeSpec)
                .build();
        javaFile.writeTo(sourceRoot);
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
        class $ {
            static final Set<String> IGNORED_PROPERTIES = Set.of(
                    // id is captured by the audited entity reference property
                    AbstractEntity.ID,
                    AbstractEntity.VERSION,
                    AbstractPersistentEntity.CREATED_BY,
                    AbstractPersistentEntity.CREATED_DATE,
                    AbstractPersistentEntity.CREATED_TRANSACTION_GUID,
                    AbstractPersistentEntity.LAST_UPDATED_BY,
                    AbstractPersistentEntity.LAST_UPDATED_DATE,
                    AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID,
                    ActivatableAbstractEntity.REF_COUNT);
        }

        return !$.IGNORED_PROPERTIES.contains(property.name());
    }

    private final class AuditEntityBuilder {
        private final ClassName className;
        private final Class<? extends AbstractEntity<?>> auditedType;
        private final ArrayList<PropertySpec> properties;
        private final ArrayList<MethodSpec> methods;

        private AuditEntityBuilder(final ClassName className, final Class<? extends AbstractEntity<?>> auditedType) {
            this.className = className;
            this.auditedType = auditedType;
            this.properties = new ArrayList<>();
            this.methods = new ArrayList<>();
        }

        public TypeSpec build(final Processor processor) {
            final var builder = classBuilder(className)
                    .addModifiers(PUBLIC)
                    .superclass(ParameterizedTypeName.get(AbstractAuditEntity.class, auditedType))
                    .addAnnotation(AnnotationSpecs.auditFor(auditedType))
                    // TODO Meta-model is not needed. Meta-model processor needs to support a new annotation - WithoutMetaModel.
                    .addAnnotation(MapEntityTo.class);
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

        interface Processor {
            PropertySpec processProperty(AuditEntityBuilder builder, PropertySpec property);
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Audit-prop entity generation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private void generateAuditPropEntity(
            final Class<? extends AbstractEntity<?>> auditedType,
            final TypeName auditEntityClassName,
            final String auditPropTypePkg,
            final String auditPropTypeName,
            final Path sourceRoot)
        throws IOException
    {
        final var auditPropTypeClassName = ClassName.get(auditPropTypePkg, auditPropTypeName);

        final var builder = classBuilder(auditPropTypeClassName)
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(javaPoet.getClassName(AbstractAuditProp.class), auditEntityClassName))
                .addAnnotation(javaPoet.getAnnotation(MapEntityTo.class));

        final var auditEntityProp = propertyBuilder(uncapitalize(auditedType.getSimpleName()) + "Audit", auditEntityClassName)
                .addAnnotation(AnnotationSpecs.compositeKeyMember(1))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .build();

        // By virtue of its name, this property's accessor and setter implement abstract methods in the base type
        final var pdProp = propertyBuilder("property", ParameterizedTypeName.get(
                javaPoet.getClassName(PropertyDescriptor.class), auditEntityClassName))
                .addAnnotation(AnnotationSpecs.compositeKeyMember(2))
                .addAnnotation(javaPoet.getAnnotation(MapTo.class))
                .build();

        PropertySpec.addProperties(environment, builder, auditPropTypeClassName, auditEntityProp, pdProp);

        // Abstract methods in the base type

        // AE getAuditedEntity();
        final var auditEntityPropAccessor = auditEntityProp.getAccessorSpec(environment);
        builder.addMethod(methodBuilder("getAuditedEntity")
                                  .addModifiers(PUBLIC)
                                  .returns(auditEntityProp.type())
                                  .addStatement("return %s()".formatted(auditEntityPropAccessor.name))
                                  .build());
        // AbstractAuditProp<AE> setAuditedEntity(AE entity);
        final var auditEntityPropSetter = auditEntityProp.getSetterSpec(environment, auditPropTypeClassName);
        builder.addMethod(methodBuilder("setAuditedEntity")
                                  .addModifiers(PUBLIC)
                                  .returns(auditPropTypeClassName)
                                  .addParameter(auditEntityClassName, "entity", FINAL)
                                  .addStatement("return %s(entity)".formatted(auditEntityPropSetter.name))
                                  .build());

        final var typeSpec = builder.build();

        final var javaFile = JavaFile.builder(auditPropTypePkg, typeSpec)
                .build();
        javaFile.writeTo(sourceRoot);
    }

    private static Path classNameToFilePath(final String packageName, final String classSimpleName) {
        return Path.of(packageName.replace('.', '/'), classSimpleName + ".java");
    }

}
