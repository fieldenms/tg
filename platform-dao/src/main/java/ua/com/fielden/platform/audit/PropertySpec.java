package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static javax.lang.model.element.Modifier.*;

final class PropertySpec {

    private final String name;
    private final Type type;
    private final List<AnnotationSpec> annotations;

    private PropertySpec(final String name, final Type type, final Iterable<? extends AnnotationSpec> annotations) {
        this.name = name;
        this.type = type;
        this.annotations = ImmutableList.copyOf(annotations);
    }

    private PropertySpec(final String name, final Type type) {
        this(name, type, ImmutableList.of());
    }

    public static Builder propertyBuilder(final String name, final Type type) {
        return new Builder(name, type, ImmutableList.of());
    }

    public Builder toBuilder() {
        return new Builder(name, type, annotations);
    }

    public FieldSpec toFieldSpec() {
        return FieldSpec.builder(type, name, PRIVATE)
                .addAnnotation(IsProperty.class)
                .addAnnotations(annotations)
                .build();
    }

    public MethodSpec getAccessorSpec() {
        final String prefix = TypeName.BOOLEAN.equals(type) ? "is" : "get";
        return MethodSpec.methodBuilder(prefix + StringUtils.capitalize(name))
                .addModifiers(PUBLIC)
                .returns(type)
                .addStatement("return this.%s".formatted(name))
                .build();
    }

    public MethodSpec getSetterSpec(final TypeName declaringClassName) {
        return MethodSpec.methodBuilder("set" + StringUtils.capitalize(name))
                .addModifiers(PUBLIC)
                .returns(declaringClassName)
                .addAnnotation(Observable.class)
                .addParameter(type, name, FINAL)
                .addStatement("this.%s = %s".formatted(name, name))
                .addStatement("return this")
                .build();
    }

    public MethodSpec getSetterSpec(final Type declaringType) {
        return getSetterSpec(TypeName.get(declaringType));
    }

    public boolean hasAnnotation(final ClassName className) {
        return annotations.stream().anyMatch(annot -> className.equals(annot.type));
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    public List<AnnotationSpec> annotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
               || obj instanceof PropertySpec that
                  && Objects.equals(this.name, that.name)
                  && Objects.equals(this.type, that.type)
                  && Objects.equals(this.annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, annotations);
    }

    @Override
    public String toString() {
        return "PropertySpec[" +
               "name=" + name + ", " +
               "type=" + type + ", " +
               "annotations=" + annotations + ']';
    }

    static final class Builder {

        private final String name;
        private final Type type;
        private final ImmutableList.Builder<AnnotationSpec> annotations;

        private Builder(final String name, final Type type, final Iterable<? extends AnnotationSpec> annotations) {
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

        public PropertySpec build() {
            return new PropertySpec(name, type, annotations.build());
        }

    }

}
