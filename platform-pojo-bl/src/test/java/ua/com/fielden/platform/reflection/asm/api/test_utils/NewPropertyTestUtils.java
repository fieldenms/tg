package ua.com.fielden.platform.reflection.asm.api.test_utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.junit.Assert;

import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.entity.annotation.Generated;

/**
 * A utility class for unit tests that use {@link NewProperty} API.
 * 
 * @author TG Team
 *
 */
public class NewPropertyTestUtils {

    /**
     * Asserts that annotations described by {@code prototype} are present on {@code field} and have the same contents.
     * It is acceptable for {@code field} to contain annotations that <b>are not</b> described by {@code prototype}.
     * 
     * @param prototype
     * @param field
     */
    public static void assertAnnotationsEquals(final NewProperty<?> prototype, final Field field) {
        prototype.getAnnotations().forEach(protoAnnot -> {
            final Annotation fieldAnnot = AnnotationReflector.getAnnotation(field, protoAnnot.annotationType());
            assertNotNull("Property %s.%s is missing an annotation: %s.".formatted(
                    field.getDeclaringClass(), field.getName(), fieldAnnot.annotationType().getName()), fieldAnnot); 
            // NOTE: It's important to use the equals method of fieldAnnot specifically, since it was obtained through reflective
            // mechanisms that involve sun.reflect.annotation.AnnotationInvocationHandler, which provides an implementation of equals.
            // Otherwise, protoAnnot is simply an anonymous type created by NewProperty that has no proper equals method.
            assertTrue("Property %s.%s has incorrect annotation contents.".formatted(
                    field.getDeclaringClass(), field.getName()), fieldAnnot.equals(protoAnnot));
        });
    }

    /**
     * Asserts that {@code prototype} is a correct representation of {@code field} by testing its name, type and annotations.
     * <p>
     * Annotations are tested by asserting that annotations described by {@code prototype} are present on {@code field} 
     * and have the same contents. It is acceptable for {@code field} to contain annotations that <b>are not</b> described by
     * {@code prototype}.
     * 
     * @param field
     * @param prototype
     */
    public static void assertPropertyEquals(final NewProperty<?> prototype, final Field field) {
        assertEquals("Incorrect property name.", prototype.getName(), field.getName());

        final Type fieldGenericType = field.getGenericType();
        if (ParameterizedType.class.isInstance(fieldGenericType)) {
            final var fieldParamType = (ParameterizedType) fieldGenericType;
            assertEquals("Incorrect property raw type.", prototype.getRawType(), fieldParamType.getRawType());
            assertEquals("Incorrect type arguments.", 
                    prototype.getTypeArguments().stream().map(Type::toString).toList(),
                    Arrays.stream(fieldParamType.getActualTypeArguments()).map(Type::toString).toList());
        }
        else {
            assertEquals("Incorrect property type.", prototype.getRawType(), PropertyTypeDeterminator.classFrom(fieldGenericType));
        }

        assertAnnotationsEquals(prototype, field);
    }
}
