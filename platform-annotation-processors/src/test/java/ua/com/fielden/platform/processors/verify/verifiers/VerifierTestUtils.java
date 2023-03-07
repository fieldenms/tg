package ua.com.fielden.platform.processors.verify.verifiers;

import java.lang.reflect.Type;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.processors.test_utils.Compilation;

public class VerifierTestUtils {

    /**
     * A convenient method that replaces 2 repetitive lines of code.
     * @param compilation
     * @return
     * @throws Throwable
     */
    public static boolean compileAndPrintDiagnostics(final Compilation compilation) {
        final boolean success = compilation.compile();
        compilation.printDiagnostics();
        return success;
    }

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
