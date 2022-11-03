package ua.com.fielden.platform.processors.verify.verifiers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.processors.test_utils.Compilation;

public class VerifierTestUtils {

    /**
     * A convenient method that replaces 2 repetitive lines of code.
     * @param compilation
     * @return
     * @throws Throwable
     */
    public static boolean compileAndPrintDiagnostics(final Compilation compilation) throws Throwable {
        final boolean success = compilation.compile();
        compilation.printDiagnostics();
        return success;
    }

    /**
     * Asserts that an error containing {@code msg} was reported during compilation.
     * @param compilation
     * @param msg
     */
    public static void assertErrorReported(final Compilation compilation, final String msg) {
        final List<Diagnostic<? extends JavaFileObject>> errors = compilation.getErrors();
        assertFalse("An error should have been reported.", errors.isEmpty());
        assertTrue("No error with a matching message was reported.",
                compilation.getErrors().stream().anyMatch(err -> msg.equals(err.getMessage(Locale.getDefault()))));
    }
    
    public static FieldSpec buildProperty(final Type type, final String name, final Annotation... annotations) {
        return FieldSpec.builder(type, name, Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(IsProperty.class).build())
                .addAnnotations(Arrays.asList(annotations).stream().map(annot -> AnnotationSpec.get(annot, true)).toList())
                .build();
    }

}
