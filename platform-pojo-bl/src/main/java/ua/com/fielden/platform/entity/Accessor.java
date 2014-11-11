package ua.com.fielden.platform.entity;

import java.lang.reflect.Method;

/**
 * Enumeration describing valid property accessors.
 * 
 * @author TG Team
 * 
 */
public enum Accessor {
    GET("get"), IS("is");

    public final String startsWith;

    /**
     * Identifies whether the specified method is an accessor.
     * 
     * @param method
     * @return
     */
    public static boolean isAccessor(final Method method) {
        return isAccessor(method.getName());
    }

    /**
     * Identifies whether the specified method name indicates an accessor.
     * 
     * @param method
     * @return
     */
    public static boolean isAccessor(final String methodName) {
        return methodName.startsWith(GET.startsWith) || //
                methodName.startsWith(IS.startsWith);
    }

    /**
     * Determines enumeration values corresponding to a method.
     * 
     * @param method
     * @return
     */
    public static Accessor getValueByMethod(final Method method) {
        if (method.getName().startsWith(GET.startsWith)) {
            return GET;
        }
        if (method.getName().startsWith(IS.startsWith)) {
            return IS;
        }
        throw new IllegalArgumentException("Method " + method.getName() + " is not a mutator.");
    }

    /**
     * Deduces property name using the provided accessor.
     * 
     * @param method
     * @return
     */
    public static String deducePropertyNameFromAccessor(final Method method) {
        return deducePropertyNameFromAccessor(method.getName());
    }

    /**
     * Deduces property name using the provided accessor name.
     * 
     * @param method
     * @return
     */
    public static String deducePropertyNameFromAccessor(final String methodName) {
        if (methodName.startsWith(GET.startsWith)) {
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        }
        if (methodName.startsWith(IS.startsWith)) {
            return methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
        }
        throw new IllegalArgumentException("Method " + methodName + " is not a mutator.");
    }

    /**
     * Deduces accessor name for the specified property.
     * 
     * @return
     */
    public String getName(final String propertyName) {
        return startsWith + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    Accessor(final String startsWith) {
        this.startsWith = startsWith;
    }
    
}
