package ua.com.fielden.platform.reflection.asm.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A convenient class for describing annotation to be generated for a dynamic field at runtime.
 *
 * @author TG Team
 *
 */
public class AnnotationDescriptor {
    public final Class<? extends Annotation> type;
    public final Map<String, Object> params = new HashMap<String, Object>();

    /**
     * Instantiates the class. Only not null values for existing parameters of the specified annotation type are considered.
     *
     * @param type
     * @param params
     */
    public AnnotationDescriptor(final Class<? extends Annotation> type, final Map<String, Object> params) {
	if (type == null) {
	    throw new IllegalArgumentException("Annotation class cannot be null.");
	}
	this.type = type;

	final Method[] methods = type.getDeclaredMethods();

	for (final Method method : methods) {
	    final Object value = params.get(method.getName());
	    if (value != null) {
		this.params.put(method.getName(), value);
	    }
	}
    }

}
