package ua.com.fielden.platform.reflection.asm.impl;

import java.lang.reflect.Field;
import java.util.List;

import ua.com.fielden.platform.entity.annotation.Generated;

/**
 * A utility class providing various methods for working with dynamically generated types. 
 * 
 * @author TG Team
 */
public class DynamicTypeUtils {

    /**
     * Determines whether a field is a generated one, i.e., a dynamically added/modified field.
     * 
     * @param field
     * @return
     */
    public static boolean isGenerated(final Field field) {
        return field.isAnnotationPresent(Generated.class);
    }

    /**
     * Returns a new list of properties ordered ascendingly according to the following rules:
     * <ul>
     *  <li>A generated property is greater than a non-generated one.</li>
     *  <li>Non-generated properties are equal.</li>
     *  <li>
     *  If both properties are generated, then their declaring classes are compared.
     *  The class that is higher in the type hierarchy is considered greater (i.e. parent > child).
     *  </li>
     * </ul>
     * @param properties - a list of properties to reorder (remains unmodified)
     * @return a new list of ordered properties
     */
    public static List<Field> orderedProperties(final List<Field> properties) {
        // sort fieldsAndKeys to achieve an order, where dynamically added properties are last
        // additionally, sort dynamically added properties by their declaring class hierarchy ascending from child
        return properties.stream().sorted((field1, field2) -> {
            final Class<?> field1Owner = field1.getDeclaringClass();
            final Class<?> field2Owner = field2.getDeclaringClass();
            // quick comparison first
            if (field1Owner.equals(field2Owner)) /*field1 == field2*/ return 0;

            final boolean field1Generated = DynamicTypeUtils.isGenerated(field1);
            final boolean field2Generated = DynamicTypeUtils.isGenerated(field2);

            if (field1Generated && field2Generated) {
                // field1Owner is equal to (already checked) or a superclass of field2Owner
                if (field1Owner.isAssignableFrom(field2Owner))      /*field1 < field2*/  return -1;
                // field1Owner is a child of field2Owner
                else if (field2Owner.isAssignableFrom(field1Owner)) /*field1 > field2*/  return 1;
                // unrelated type hierarchies (impossible scenario)
                else                                                /*field1 == field2*/ return 0;
            }
            // both are not generated (fields that exist at compile-time)
            else if (!field1Generated && !field2Generated) /*field1 == field2*/ return 0;
            // only field1 is generated
            else if (field1Generated)                      /*field1 > field2*/  return 1;
            // only field2 is generated
            else                                           /*field1 < field2*/  return -1;
        }).toList();
    }

}
