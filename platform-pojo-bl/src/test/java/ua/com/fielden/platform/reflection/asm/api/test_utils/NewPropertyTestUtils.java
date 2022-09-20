package ua.com.fielden.platform.reflection.asm.api.test_utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;

/**
 * A utility class for unit tests that use {@link NewProperty} API.
 * 
 * @author TG Team
 *
 */
public class NewPropertyTestUtils {

    /**
     * Asserts that annotations present on {@code property} are equal to those described by the {@code prototype}.
     * 
     * @param prototype
     * @param property
     */
    public static void assertAnnotationsEquals(final NewProperty<?> prototype, final Field property) {
        prototype.getAnnotations().forEach(protoAnnot -> {
            final Annotation annot = AnnotationReflector.getAnnotation(property, protoAnnot.annotationType());
            assertNotNull("Property %s.%s is missing an annotation: %s.".formatted(
                    property.getDeclaringClass(), property.getName(), annot.annotationType().getName()), annot); 
            // NOTE: It's important to use the equals method of annot specifically, since it was obtained through reflective
            // mechanisms that involve sun.reflect.annotation.AnnotationInvocationHandler, which provides an implementation of equals.
            // Otherwise, protoAnnot is simply an anonymous type created by NewProperty that has no proper equals method.
            assertTrue("Property %s.%s has incorrect annotation contents.".formatted(
                    property.getDeclaringClass(), property.getName()), annot.equals(protoAnnot));
        });
    }
}
