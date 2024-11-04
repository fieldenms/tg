package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static javax.lang.model.element.Modifier.*;

final class PropertySpec {

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

        if (initializer != null) {
            builder.initializer(initializer);
        }

        ifCollectionalOrElse(environment,
                             (collTypeName, eltTypeName) -> {
                                 if (initializer == null) {
                                     throw new IllegalStateException("Collectional property must have an initialiser.");
                                 }

                                 builder.addAnnotation(AnnotationSpec.builder(IsProperty.class)
                                                               .addMember("value", "$T.class", eltTypeName)
                                                               .build())
                                         .addModifiers(FINAL);
                             },
                             () -> ifPropertyDescriptorOrElse(environment,
                                                              entityTypeName ->
                                                                      builder.addAnnotation(AnnotationSpec.builder(IsProperty.class)
                                                                                                    .addMember("value", "$T.class", entityTypeName)
                                                                                                    .build()),
                                                              () -> builder.addAnnotation(environment.javaPoet().getAnnotation(IsProperty.class))));

        return builder.build();
    }

    public MethodSpec getAccessorSpec(final GeneratorEnvironment environment) {
        final String prefix = TypeName.BOOLEAN.equals(typeName) ? "is" : "get";
        final var builder = MethodSpec.methodBuilder(prefix + StringUtils.capitalize(name))
                .addModifiers(PUBLIC)
                .returns(typeName);

        ifCollectionalOrElse(environment,
                             (collTypeName, eltTypeName) -> {
                                    final var collectionsMethodName = switch (CollectionType.fromName(collTypeName.canonicalName())) {
                                        case LIST -> "unmodifiableList";
                                        case SET -> "unmodifiableSet";
                                    };
                                 builder.addStatement("return $T.%s(this.%s)".formatted(collectionsMethodName, name), Collections.class);
                             },
                             () -> builder.addStatement("return this.%s".formatted(name)));

        return builder.build();
    }

    public MethodSpec getSetterSpec(final GeneratorEnvironment environment, final TypeName declaringClassName) {
        final var builder = MethodSpec.methodBuilder("set" + StringUtils.capitalize(name))
                .addModifiers(PUBLIC)
                .returns(declaringClassName)
                .addAnnotation(Observable.class)
                .addParameter(typeName, name, FINAL);

        ifCollectionalOrElse(environment,
                             (collTypeName, eltTypeName) -> builder
                                     .addStatement("this.%s.clear()".formatted(name))
                                     .addStatement("this.%s.addAll(%s)".formatted(name, name))
                                     .addStatement("return this"),
                             () -> builder
                                     .addStatement("this.%s = %s".formatted(name, name))
                                     .addStatement("return this"));

        return builder.build();
    }

    public MethodSpec getSetterSpec(final GeneratorEnvironment environment, final Type declaringType) {
        return getSetterSpec(environment, TypeName.get(declaringType));
    }

    public boolean hasAnnotation(final ClassName className) {
        return annotations.stream().anyMatch(annot -> className.equals(annot.type));
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

    private void ifCollectionalOrElse(
            final GeneratorEnvironment environment,
            final BiConsumer<? super ClassName, ? super TypeName> collectionalAction,
            final Runnable elseAction)
    {
        if (typeName instanceof ParameterizedTypeName paramTypeName && paramTypeName.typeArguments.size() == 1) {
            Optional.ofNullable(environment.javaPoet().reflectType(paramTypeName.rawType))
                    .filter(EntityUtils::isCollectional)
                    .ifPresentOrElse($ -> collectionalAction.accept(paramTypeName.rawType, paramTypeName.typeArguments.getFirst()),
                                     elseAction);
        } else {
            elseAction.run();
        }
    }

    private void ifPropertyDescriptorOrElse(
            final GeneratorEnvironment environment,
            final Consumer<? super TypeName> propDescriptorAction,
            final Runnable elseAction)
    {
        if (typeName instanceof ParameterizedTypeName paramTypeName && paramTypeName.typeArguments.size() == 1) {
            Optional.ofNullable(environment.javaPoet().reflectType(paramTypeName.rawType))
                    .filter(EntityUtils::isPropertyDescriptor)
                    .ifPresentOrElse($ -> propDescriptorAction.accept(paramTypeName.typeArguments.getFirst()),
                                     elseAction);
        } else {
            elseAction.run();
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
