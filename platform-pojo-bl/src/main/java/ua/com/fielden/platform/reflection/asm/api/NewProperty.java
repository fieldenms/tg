package ua.com.fielden.platform.reflection.asm.api;

import java.lang.annotation.Annotation;


/**
 * A convenient abstraction for representing data needed for dynamic construction of properties.
 *
 * @author TG Team
 *
 */
public final class NewProperty {
    public final String name;
    public final Class<?> type;
    public final boolean changeSignature;
    public final String title;
    public final String desc;
    public final AnnotationDescriptor[] annotations;

    public static NewProperty changeType(final String name, final Class<?> type) {
	return new NewProperty (name, type, false, null, null, new AnnotationDescriptor[0]);
    }

    public static NewProperty changeTypeSignature(final String name, final Class<?> type) {
	return new NewProperty (name, type, true, null, null, new AnnotationDescriptor[0]);
    }

    public NewProperty(final String name, final Class<?> type, final boolean changeSignature, final String title, final String desc, final AnnotationDescriptor... annotations) {
	this.name = name;
	this.type = type;
	this.changeSignature = changeSignature;
	this.title = title;
	this.desc = desc;
	this.annotations = annotations;
    }

    /**
     * Tests whether an annotation descriptor is present for a specific annotation type.
     *
     * @param annotationType
     * @return
     */
    public boolean containsAnnotationDescriptorFor(final Class<? extends Annotation> annotationType) {
	return getAnnotationDescriptorByType(annotationType) != null;
    }

    /**
     * Returns an annotation description for the specified annotation type. Returns <code>null</code> if such description is not in the list.
     *
     * @param annotationType
     * @return
     */
    public AnnotationDescriptor getAnnotationDescriptorByType(final Class<? extends Annotation> annotationType) {
	for (final AnnotationDescriptor ad : annotations) {
	    if (ad.type == annotationType) {
		return ad;
	    }
	}
	return null;
    }
}
