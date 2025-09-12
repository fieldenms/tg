package ua.com.fielden.platform.processors.verify.verifiers;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Type;

public class VerifierTestUtils {

    /**
     * Returns a field builder for entity properties that already includes a {@code private} modifier and {@link IsProperty} annotation.
     */
    public static FieldSpec.Builder propertyBuilder(final Type type, final String name) {
        return FieldSpec.builder(type, name, Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(IsProperty.class).build());
    }

    /**
     * Returns a field builder for entity properties that already includes a {@code private} modifier and {@link IsProperty} annotation.
     */
    public static FieldSpec.Builder propertyBuilder(final TypeName typeName, final String name) {
        return FieldSpec.builder(typeName, name, Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(IsProperty.class).build());
    }

    /**
     * Returns a method builder for a property setter having the following form:
     * <pre>
     * {@literal @Observable}
     * public $entityType $name($propType x) {}
     * </pre>
     */
    public static MethodSpec.Builder setterBuilder(final String name, final TypeName propType, final TypeName entityType) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(entityType)
                .addParameter(propType, "x")
                .addAnnotation(Observable.class);
    }

}
