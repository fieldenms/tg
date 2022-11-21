package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.reflection.asm.api.test_utils.NewPropertyTestUtils.assertPropertyEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;

public class DynamicEntityTypeTestUtils {
    
    public static <T extends AbstractEntity<?>> T assertInstantiation(final Class<T> type, final EntityFactory factory) {
        final T instance = factory.newEntity(type);
        assertNotNull("Could not instantiate entity type %s.".formatted(type.getName()), instance);
        return instance;
    }

    // TODO private?
    public static List<Type> extractTypeArguments(final Type type) {
        if (ParameterizedType.class.isInstance(type)) {
            return Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
        }
        else return List.of();
    }
    
    public static Field assertFieldExists(final Class<?> owningType, final String name) {
        final Field field;
        try {
            field = Finder.getFieldByName(owningType, name);
        } catch (final Exception e) {
            fail("Field %s was not found in type %s.".formatted(name, owningType.getName()));
            return null;
        }

        return field;
    }
    
    public static Field assertFieldDeclared(final Class<?> owningType, final String name) {
        final Field field;
        try {
            field = owningType.getDeclaredField(name);
        } catch (final Exception e) {
            fail("Field %s was not found in type %s.".formatted(name, owningType.getName()));
            return null;
        }

        return field;
    }
    
    /**
     * Asserts that two fields are equal, ignoring their declaring classes. 
     * <p>
     * For more details refer to {@link #assertFieldEquals(Field, Field, boolean)}.
     * @param expected
     * @param actual
     */
    public static void assertFieldEqualsIgnoringOwner(final Field expected, final Field actual) {
        assertFieldEquals(expected, actual, true);
    }
    
    /**
     * Asserts that two fields are equal. 
     * It is required that annotations declared by {@code actual} constitute a superset
     * of those declared by {@code expected}.
     * @param expected
     * @param actual
     * @param ignoreOwner indicates whether to ignore the declaring class of a field
     */
    private static void assertFieldEquals(final Field expected, final Field actual, final boolean ignoreOwner) {
        if (!ignoreOwner) {
            assertEquals("", expected.getDeclaringClass(), actual.getDeclaringClass());
        }
        assertEquals("Incorrect field name.", expected.getName(), actual.getName());
        assertEquals("Incorrect field generic type.", expected.getGenericType(), actual.getGenericType());
        assertAnnotatedWith(actual, expected.getDeclaredAnnotations());
    }

    /**
     * Asserts that {@code generatedType} contains a property being modeled by {@code prototype}. Assertion is successful if:
     * <ul>
	 *   <li>{@code generatedType} contains the field in question</li>
	 *   <li>The field's raw type and type arguments are equal to those modeled by {@code prototype}</li>
	 *   <li>The field is annotated with all annotations modeled by {@code prototype} and their contents are equal</li>
	 *   <li>{@code generatedType} contains a respective accessor method with correct signature</li>
	 *   <li>{@code generatedType} contains a respective setter method with correct signature</li>
	 * </ul>
     * @param prototype
     * @param generatedType
     * @return
     */
    public static Field assertGeneratedPropertyCorrectness(final NewProperty<?> prototype, final Class<?> generatedType) {
        final String name = prototype.getName();
        final Field field = assertFieldExists(generatedType, name);

        assertPropertyEquals(prototype, field);
        assertGeneratedPropertyAccessorSignature(prototype, generatedType);
        assertGeneratedPropertySetterSignature(prototype, generatedType);

        return field;
    }
    
    public static Method assertGeneratedPropertyAccessorSignature(final NewProperty<?> prototype, final Class<?> generatedType) {
        final String name = prototype.getName();

        final Method accessor;
        try {
            accessor = Reflector.obtainPropertyAccessor(generatedType, name);
        } catch (final Exception e) {
            fail("Accessor method for modified collectional property %s was not found.".formatted(name));
            return null;
        }

        assertEquals("Incorrect number of accessor parameters.", 0, accessor.getParameterCount());
        assertEquals("Incorrect accessor return raw type.", prototype.getRawType(), accessor.getReturnType());
        assertEquals("Incorrect accessor return type arguments.", 
                prototype.getTypeArguments(), extractTypeArguments(accessor.getGenericReturnType()));
        
        return accessor;
    }

    public static Method assertGeneratedPropertySetterSignature(final NewProperty<?> prototype, final Class<?> generatedType) {
        final String name = prototype.getName();

        final Method setter;
        try {
            setter = Reflector.obtainPropertySetter(generatedType, name);
        } catch (final Exception e) {
            fail("Setter method for modified collectional property %s was not found.".formatted(name));
            return null;
        }

        assertEquals("Incorrect number of setter parameters.", 1, setter.getParameterCount());
        assertEquals("Incorrect setter parameter raw type.", prototype.getRawType(), setter.getParameterTypes()[0]);
        assertEquals("Incorrect setter parameter type arguments.", 
                prototype.getTypeArguments(), extractTypeArguments(setter.getGenericParameterTypes()[0]));
        assertEquals("Incorrect setter return type", generatedType, setter.getReturnType());

        return setter;
    }
    
    public static void assertAnnotatedWith(final AnnotatedElement element, final Collection<Annotation> annotations) {
        final List<Annotation> declaredAnnotations = Arrays.asList(element.getDeclaredAnnotations());
        annotations.forEach(annot -> assertTrue("Element %s is missing declared annotation %s.".formatted(element.toString(), annot.toString()),
                declaredAnnotations.contains(annot)));
    }
    
    public static void assertAnnotatedWith(final AnnotatedElement element, final Annotation... annotations) {
        assertAnnotatedWith(element, Arrays.asList(annotations));
    }
    
    
    /**
     * Asserts that {@code actual} has all properties that are found in {@code expected}. 
     * The whole class hierarchy is traversed for both entity types.
     * @param expected
     * @param actual
     */
    public static void assertHasProperties(final Class<? extends AbstractEntity<?>> expected, final Class<? extends AbstractEntity<?>> actual) {
        final List<Field> actualProperties = Finder.findProperties(actual);

        Finder.findProperties(expected).forEach(expectedProp -> {
            final Field actualProp = actualProperties.stream()
                    .filter(prop -> prop.getName().equals(expectedProp.getName()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "Expected property \"%s\" wasn't found in the actual type."
                            .formatted(expectedProp.getName())));
            assertFieldEqualsIgnoringOwner(expectedProp, actualProp);
        });
    }

}