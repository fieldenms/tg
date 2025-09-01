package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

import jakarta.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.audit.JavaPoet.annotationMember;
import static ua.com.fielden.platform.types.tuples.T2.t2;

final class PropertySpec {

    private static final TypeName TYPE_NAME_IS_PROPERTY = TypeName.get(IsProperty.class);
    private static final TypeName TYPE_NAME_TITLE = TypeName.get(Title.class);

    private final String name;
    private final TypeName typeName;
    private final List<AnnotationSpec> annotations;
    private final @Nullable CodeBlock initializer;

    private PropertySpec(final String name, final TypeName typeName, @Nullable final CodeBlock initializer, final Iterable<? extends AnnotationSpec> annotations) {
        this.name = name;
        this.typeName = typeName;
        this.initializer = initializer;
        this.annotations = ImmutableList.copyOf(annotations);
    }

    private PropertySpec(final String name, final TypeName typeName, final CodeBlock initializer) {
        this(name, typeName, initializer, ImmutableList.of());
    }

    public static Builder propertyBuilder(final String name, final TypeName type) {
        return new Builder(name, type, ImmutableList.of());
    }

    public static Builder propertyBuilder(final String name, final Type type) {
        return new Builder(name, TypeName.get(type), ImmutableList.of());
    }

    /**
     * Adds the specified property, along with its accessor and setter, to the specified builder.
     *
     * @param typeName name of the type being built (whether this is true is not checked by this method)
     */
    public static TypeSpec.Builder addProperty(
            final GeneratorEnvironment environment,
            final TypeSpec.Builder builder,
            final TypeName typeName,
            final PropertySpec propertySpec)
    {
        return builder.addField(propertySpec.toFieldSpec(environment))
                .addMethod(propertySpec.getAccessorSpec(environment))
                .addMethod(propertySpec.getSetterSpec(environment, typeName));
    }

    /**
     * Adds the specified properties, along with their accessors and setters, to the specified builder.
     *
     * @param typeName name of the type being built (whether this is true is not checked by this method)
     */
    public static TypeSpec.Builder addProperties(
            final GeneratorEnvironment environment,
            final TypeSpec.Builder builder,
            final TypeName typeName,
            final PropertySpec propertySpec,
            final PropertySpec... propertySpecs)
    {
        addProperty(environment, builder, typeName, propertySpec);
        for (final var spec : propertySpecs) {
            addProperty(environment, builder, typeName, spec);
        }
        return builder;
    }

    public Builder toBuilder() {
        return new Builder(name, typeName, annotations);
    }

    public FieldSpec toFieldSpec(final GeneratorEnvironment environment) {
        final var builder = FieldSpec.builder(typeName, name, PRIVATE)
                .addAnnotations(annotations);

        asCollectionalType(environment).ifPresent($ -> builder.addModifiers(FINAL));

        if (initializer != null) {
            builder.initializer(initializer);
        }

        // Use a default IsProperty annotation if none was specified.
        if (annotations.stream().noneMatch(a -> a.type.equals(environment.javaPoet().getTypeName(IsProperty.class)))) {
            builder.addAnnotation(mkIsPropertyAnnotation(environment));
        }

        return builder.build();
    }

    private AnnotationSpec mkIsPropertyAnnotation(final GeneratorEnvironment environment) {
        return asCollectionalType(environment)
                .map(collTy -> collTy.map((collTypeName, eltTypeName) -> {
                    if (initializer == null) {
                        throw new IllegalStateException("Collectional property must have an initialiser.");
                    }

                    return AnnotationSpec.builder(IsProperty.class)
                            .addMember("value", "$T.class", eltTypeName)
                            .build();
                }))
                .orElseGet(() -> asPropertyDescriptor(environment)
                        .map(entityTypeName -> AnnotationSpec.builder(IsProperty.class)
                                .addMember("value", "$T.class", entityTypeName)
                                .build())
                        .orElseGet(() -> environment.javaPoet().getAnnotation(IsProperty.class)));
    }

    public MethodSpec getAccessorSpec(final GeneratorEnvironment environment) {
        final String prefix = TypeName.BOOLEAN.equals(typeName) ? "is" : "get";
        final var builder = MethodSpec.methodBuilder(prefix + StringUtils.capitalize(name))
                .addModifiers(PUBLIC)
                .returns(typeName);

        asCollectionalType(environment)
                .ifPresentOrElse(collTy -> collTy.run((collTypeName, eltTypeName) -> {
                                     final var collectionsMethodName = switch (CollectionType.fromName(collTypeName.canonicalName())) {
                                         case LIST -> "unmodifiableList";
                                         case SET -> "unmodifiableSet";
                                     };
                                     builder.addStatement("return $T.$L(this.$L)", Collections.class, collectionsMethodName, name);
                                 }),
                                 () -> builder.addStatement("return this.$L", name));

        return builder.build();
    }

    /// Creates a method that represents a setter for this property.
    ///
    /// @param declaringTypeName  name of the type that declares this setter
    public MethodSpec getSetterSpec(final GeneratorEnvironment environment, final TypeName declaringTypeName) {
        return getSetterSpecWithParamType(environment, declaringTypeName, typeName);
    }

    /// Creates a method that represents a setter for this property.
    ///
    /// @param declaringTypeName  name of the type that declares this setter
    /// @param paramTypeName  name of the parameter type
    public MethodSpec getSetterSpecWithParamType(final GeneratorEnvironment environment, final TypeName declaringTypeName, final TypeName paramTypeName) {
        final var builder = MethodSpec.methodBuilder("set" + StringUtils.capitalize(name))
                .addModifiers(PUBLIC)
                .returns(declaringTypeName)
                .addAnnotation(Observable.class)
                .addParameter(paramTypeName, name, FINAL);

        asCollectionalType(environment)
                .ifPresentOrElse(collTy -> collTy.run((collTypeName, eltTypeName) -> builder
                                         .addStatement("this.$L.clear()", name)
                                         .addStatement("this.$L.addAll($L)", name, name)
                                         .addStatement("return this")),
                                 () -> builder
                                         .addStatement("this.$L = $L", name, name)
                                         .addStatement("return this"));

        return builder.build();
    }

    public boolean hasAnnotation(final ClassName className) {
        return annotations.stream().anyMatch(annot -> className.equals(annot.type));
    }

    public boolean hasAnnotation(final Class<? extends Annotation> type) {
        return hasAnnotation(ClassName.get(type));
    }

    public String name() {
        return name;
    }

    public TypeName type() {
        return typeName;
    }

    public List<AnnotationSpec> annotations() {
        return annotations;
    }

    public Optional<String> title() {
        return annotations.stream()
                .filter(a -> a.type.equals(TYPE_NAME_TITLE))
                .findFirst()
                .flatMap(as -> annotationMember(as, "value"))
                .map(CodeBlock::toString)
                // This string comes from a CodeBlock, so it will always be enclosed in double-quotes.
                .map(PropertySpec::unquote)
                .filter(s -> !s.isEmpty());
    }

    private static String unquote(final String s) {
        // We can assume that the input string will always be enclosed in double-quotes.
        return s.substring(1, s.length() - 1);
    }

    public Optional<String> desc() {
        return annotations.stream()
                .filter(a -> a.type.equals(TYPE_NAME_TITLE))
                .findFirst()
                .flatMap(as -> annotationMember(as, "desc"))
                .map(CodeBlock::toString)
                // This string comes from a CodeBlock, so it will always be enclosed in double-quotes.
                .map(PropertySpec::unquote)
                .filter(s -> !s.isEmpty());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
               || obj instanceof PropertySpec that
                  && Objects.equals(this.name, that.name)
                  && Objects.equals(this.typeName, that.typeName)
                  && Objects.equals(this.annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, typeName, annotations);
    }

    @Override
    public String toString() {
        return "PropertySpec[" +
               "name=" + name + ", " +
               "type=" + typeName + ", " +
               "annotations=" + annotations + ']';
    }

    private Optional<T2<ClassName, TypeName>> asCollectionalType(final GeneratorEnvironment environment) {
        if (typeName instanceof ParameterizedTypeName paramTypeName && paramTypeName.typeArguments.size() == 1) {
            return Optional.ofNullable(environment.javaPoet().reflectType(paramTypeName.rawType))
                    .filter(EntityUtils::isCollectional)
                    .map($ -> t2(paramTypeName.rawType, paramTypeName.typeArguments.getFirst()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<TypeName> asPropertyDescriptor(final GeneratorEnvironment environment) {
        if (typeName instanceof ParameterizedTypeName paramTypeName && paramTypeName.typeArguments.size() == 1) {
            return Optional.ofNullable(environment.javaPoet().reflectType(paramTypeName.rawType))
                    .filter(EntityUtils::isPropertyDescriptor)
                    .map($ -> paramTypeName.typeArguments.getFirst());
        } else {
            return Optional.empty();
        }
    }


    private enum CollectionType {
        LIST (List.class.getCanonicalName()),
        SET (Set.class.getCanonicalName());

        public final String canonicalName;

        CollectionType(final String canonicalName) {
            this.canonicalName = canonicalName;
        }

        public static CollectionType fromName(final CharSequence name) {
            for (final var collType : CollectionType.values()) {
                if (collType.canonicalName.contentEquals(name)) {
                    return collType;
                }
            }
            throw new InvalidArgumentException("Unrecognised collection type: %s".formatted(name));
        };
    }


    static final class Builder {

        private final String name;
        private final TypeName type;
        private final ImmutableList.Builder<AnnotationSpec> annotations;
        private @Nullable CodeBlock initializer;

        private Builder(final String name, final TypeName type, final Iterable<? extends AnnotationSpec> annotations) {
            this.name = name;
            this.type = type;
            this.annotations = ImmutableList.builderWithExpectedSize(annotations instanceof Collection c ? c.size() : 0);
            this.annotations.addAll(annotations);
        }

        public Builder addAnnotation(final AnnotationSpec annotationSpec) {
            this.annotations.add(annotationSpec);
            return this;
        }

        public Builder addAnnotation(final ClassName annotation) {
            return addAnnotation(AnnotationSpec.builder(annotation).build());
        }

        public Builder addAnnotation(final Class<? extends Annotation> annotation) {
            return addAnnotation(ClassName.get(annotation));
        }

        public Builder addAnnotations(final Iterable<AnnotationSpec> annotations) {
            this.annotations.addAll(annotations);
            return this;
        }

        public Builder initializer(final CodeBlock codeBlock) {
            this.initializer = codeBlock;
            return this;
        }

        public Builder initializer(final String format, final Object... args) {
            return initializer(CodeBlock.of(format, args));
        }

        public PropertySpec build() {
            return new PropertySpec(name, type, initializer, annotations.build());
        }

    }

}
