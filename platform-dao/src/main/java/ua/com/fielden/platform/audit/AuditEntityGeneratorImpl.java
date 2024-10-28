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
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.reflection.ClassesRetriever;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.audit.PropertySpec.propertyBuilder;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

final class AuditEntityGeneratorImpl implements AuditEntityGenerator {

    static final String A3T = "a3t";

    private final IDomainMetadata domainMetadata;
    private final Map<Type, TypeName> typeNameCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, AnnotationSpec> markerAnnotationCache = new ConcurrentHashMap<>();

    @Inject
    AuditEntityGeneratorImpl(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    @Override
    public Set<Path> generate(
            final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            final Path sourceRoot)
    {
        return Streams.stream(entityTypes)
                .parallel()
                .map(type -> generate_(type, sourceRoot))
                .collect(toImmutableSet());
    }

    private Path generate_(final Class<? extends AbstractEntity<?>> type, final Path sourceRoot) {
        final var auditTypePkg = type.getPackageName();
        // TODO multiple audit-entity versions
        final var auditTypeVersion = 1;
        final var auditTypeName = type.getSimpleName() + "_" + A3T + "_" + auditTypeVersion;
        final var auditTypePath = sourceRoot.resolve(classNameToFilePath(auditTypePkg, auditTypeName));

        try {
            generateSource(type, auditTypePkg, auditTypeName, sourceRoot);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(auditTypeVersion, type.getTypeName()), e);
        }

        return auditTypePath;
    }

    private void generateSource(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName,
            final Path sourceRoot)
        throws IOException
    {
        final var auditTypeClassName = ClassName.get(auditTypePkg, auditTypeName);

        final var a3tBuilder = new AuditEntityBuilder(auditTypeClassName, type);

        // Property for the reference to the audited entity
        final var auditedEntityProp = propertyBuilder(uncapitalize(type.getSimpleName()), type)
                .addAnnotation(AnnotationSpecs.compositeKeyMember(AbstractAuditEntity.NEXT_COMPOSITE_KEY_MEMBER))
                .addAnnotation(getAnnotation(MapTo.class))
                .addAnnotation(getAnnotation(Required.class))
                // TODO @Final?
                .addAnnotation(AnnotationSpecs.title(getEntityTitleAndDesc(type)))
                .build();

        a3tBuilder.addProperty(auditedEntityProp);

        // Abstract methods in the base audit entity type
        a3tBuilder.addMethod(methodBuilder("getAuditedEntity")
                                     .addModifiers(PUBLIC)
                                     .returns(getClassName(type))
                                     .addAnnotation(getAnnotation(Override.class))
                                     .addStatement("return %s()".formatted(auditedEntityProp.getAccessorSpec().name))
                                     .build());
        a3tBuilder.addMethod(methodBuilder("setAuditedEntity")
                                     .addModifiers(PUBLIC)
                                     .addParameter(getClassName(type), "entity", Modifier.FINAL)
                                     .returns(auditTypeClassName)
                                     .addAnnotation(getAnnotation(Override.class))
                                     .addStatement("return %s(%s)".formatted(auditedEntityProp.getSetterSpec(auditTypeClassName).name, "entity"))
                                     .build());

        // Audited properties
        final var auditedEntityMetadata = domainMetadata.forEntity(type);
        auditedEntityMetadata.properties().stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .filter(AuditEntityGeneratorImpl::isAudited)
                .map(pm -> propertyBuilder("a3t_" + pm.name(), pm.type().javaType())
                        .addAnnotation(getAnnotation(MapTo.class))
                        // TODO @Final?
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
            return reflectType(propSpec.type()) instanceof Class klass
                   && domainMetadata.forEntityOpt(klass).isPresent()
                   && !propSpec.hasAnnotation(getClassName(SkipEntityExistsValidation.class))
                    ? propSpec.toBuilder().addAnnotation(getAnnotation(SkipEntityExistsValidation.class)).build()
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

    private ClassName getClassName(final Class<?> type) {
        return (ClassName) typeNameCache.computeIfAbsent(type, ClassName::get);
    }

    private TypeName getTypeName(final Type type) {
        return typeNameCache.computeIfAbsent(type, TypeName::get);
    }

    private AnnotationSpec getAnnotation(final Class<? extends Annotation> annotationType) {
        return markerAnnotationCache.computeIfAbsent(annotationType, k -> AnnotationSpec.builder(k).build());
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
            final var builder = TypeSpec.classBuilder(className)
                    .addModifiers(PUBLIC)
                    .superclass(ParameterizedTypeName.get(AbstractAuditEntity.class, auditedType))
                    .addAnnotation(AnnotationSpecs.auditFor(auditedType))
                    // TODO Meta-model is not needed. Meta-model processor needs to support a new annotation - WithoutMetaModel.
                    .addAnnotation(MapEntityTo.class);
            properties.stream()
                    .map(prop -> processor.processProperty(this, prop))
                    .forEach(propSpec -> {
                        builder.addField(propSpec.toFieldSpec());
                        builder.addMethod(propSpec.getAccessorSpec());
                        builder.addMethod(propSpec.getSetterSpec(className));
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

    private static Path classNameToFilePath(final String packageName, final String classSimpleName) {
        return Path.of(packageName.replace('.', '/'), classSimpleName + ".java");
    }

    /**
     * Converts the named type to a Java reflection object, if such type exists; otherwise, returns {@code null}.
     * <p>
     * Limitations:
     * <ul>
     *   <li> Unsupported types: arrays, wildcards, type variables.
     *   <li> For parameterised type names, only the raw type is used.
     * </ul>
     */
    private static @Nullable Type reflectType(final TypeName typeName) {
        class $ {
            static final Map<TypeName, Class<?>> PRIMITIVES = Map.of(
                    TypeName.VOID, void.class,
                    TypeName.BOOLEAN, boolean.class,
                    TypeName.BYTE, byte.class,
                    TypeName.SHORT, short.class,
                    TypeName.INT, int.class,
                    TypeName.LONG, long.class,
                    TypeName.CHAR, char.class,
                    TypeName.FLOAT, float.class,
                    TypeName.DOUBLE, double.class);
        }

        if (typeName.isPrimitive()) {
            return $.PRIMITIVES.get(typeName);
        }
        else {
            return switch (typeName) {
                // TODO findClass will throw if a class is not found. Introduce ClassesRetriever.findClassOrNull.
                case ClassName className -> ClassesRetriever.findClass(className.reflectionName());
                case ParameterizedTypeName paramTypeName -> reflectType(paramTypeName.rawType);
                default -> throw new UnsupportedOperationException("Type name [%s] cannot be converted to a reflected type.");
            };
        }
    }

}
