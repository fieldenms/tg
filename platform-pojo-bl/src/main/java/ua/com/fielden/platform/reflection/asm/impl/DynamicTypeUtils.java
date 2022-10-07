package ua.com.fielden.platform.reflection.asm.impl;

import java.lang.reflect.Field;

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

}
