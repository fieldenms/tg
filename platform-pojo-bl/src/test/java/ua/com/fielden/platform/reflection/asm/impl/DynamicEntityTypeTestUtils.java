package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.reflection.asm.api.test_utils.NewPropertyTestUtils.assertPropertyEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

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

        final Method accessor = Reflector.obtainPropertyAccessor(generatedType, name);
        assertEquals("Incorrect number of accessor parameters.", 0, accessor.getParameterCount());
        assertEquals("Incorrect accessor return raw type.", prototype.getRawType(), accessor.getReturnType());
        assertEquals("Incorrect accessor return type arguments.", 
                prototype.getTypeArguments(), extractTypeArguments(accessor.getGenericReturnType()));

        final Method setter = Reflector.obtainPropertySetter(generatedType, name);
        assertEquals("Incorrect number of setter parameters.", 1, setter.getParameterCount());
        assertEquals("Incorrect setter parameter raw type.", prototype.getRawType(), setter.getParameterTypes()[0]);
        assertEquals("Incorrect setter parameter type arguments.", 
                prototype.getTypeArguments(), extractTypeArguments(setter.getGenericParameterTypes()[0]));

        return field;
    }

}