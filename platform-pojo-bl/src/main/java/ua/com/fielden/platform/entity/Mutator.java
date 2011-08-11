package ua.com.fielden.platform.entity;

import java.lang.reflect.Method;

/**
 * Enumeration describing valid property mutators.
 *
 * @author TG Team
 *
 */
public enum Mutator {
    SETTER("set"), INCREMENTOR("addTo"), DECREMENTOR("removeFrom");

    public final String startsWith;

    /**
     * Identifies whether the specified method is a mutator.
     *
     * @param method
     * @return
     */
    public static boolean isMutator(final Method method) {
	return isMutator(method.getName());
    }

    /**
     * Identifies whether the specified method name indicates a mutator.
     *
     * @param method
     * @return
     */
    public static boolean isMutator(final String methodName) {
	return methodName.startsWith(SETTER.startsWith) || //
		methodName.startsWith(INCREMENTOR.startsWith) || //
		methodName.startsWith(DECREMENTOR.startsWith);
    }


    /**
     * Determines enumeration values corresponding to a method.
     *
     * @param method
     * @return
     */
    public static Mutator getValueByMethod(final Method method) {
	if (method.getName().startsWith(SETTER.startsWith)) {
	    return SETTER;
	}
	if (method.getName().startsWith(INCREMENTOR.startsWith)) {
	    return INCREMENTOR;
	}
	if (method.getName().startsWith(DECREMENTOR.startsWith)) {
	    return DECREMENTOR;
	}
	throw new IllegalArgumentException("Method " + method.getName() + " is not a mutator.");
    }

    /**
     * Deduces property name using the provided mutator.
     *
     * @param method
     * @return
     */
    public static String deducePropertyNameFromMutator(final Method method) {
	return deducePropertyNameFromMutator(method.getName());
    }

    /**
     * Deduces property name using the provided mutator name.
     *
     * @param method
     * @return
     */
    public static String deducePropertyNameFromMutator(final String methodName) {
	if (methodName.startsWith(SETTER.startsWith)) {
	    return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
	}
	if (methodName.startsWith(INCREMENTOR.startsWith)) {
	    return methodName.substring(5, 6).toLowerCase() + methodName.substring(6);
	}
	if (methodName.startsWith(DECREMENTOR.startsWith)) {
	    return methodName.substring(10, 11).toLowerCase() + methodName.substring(11);
	}
	throw new IllegalArgumentException("Method " + methodName + " is not a mutator.");
    }


    /**
     * Deduces mutator name for the specified property.
     *
     * @return
     */
    public String getName(final String propertyName) {
	return startsWith + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    Mutator(final String startsWith) {
	this.startsWith = startsWith;
    }
}
