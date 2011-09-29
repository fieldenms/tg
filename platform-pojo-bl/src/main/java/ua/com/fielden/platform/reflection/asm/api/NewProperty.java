package ua.com.fielden.platform.reflection.asm.api;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
    public final List<Annotation> annotations = new ArrayList<Annotation>();

    public static NewProperty changeType(final String name, final Class<?> type) {
	return new NewProperty (name, type, false, null, null, new Annotation[0]);
    }

    public static NewProperty changeTypeSignature(final String name, final Class<?> type) {
	return new NewProperty (name, type, true, null, null, new Annotation[0]);
    }

    public NewProperty(final String name, final Class<?> type, final boolean changeSignature, final String title, final String desc, final Annotation... annotations) {
	this.name = name;
	this.type = type;
	this.changeSignature = changeSignature;
	this.title = title;
	this.desc = desc;
	this.annotations.addAll(Arrays.asList(annotations));
    }

    /**
     * Tests whether an annotation is present for a specific annotation type.
     *
     * @param annotationType
     * @return
     */
    public boolean containsAnnotationDescriptorFor(final Class<? extends Annotation> annotationType) {
	return getAnnotationByType(annotationType) != null;
    }

    /**
     * Returns an annotation for the specified annotation type. Returns <code>null</code> if such annotation is not in the list.
     *
     * @param annotationType
     * @return
     */
    public Annotation getAnnotationByType(final Class<? extends Annotation> annotationType) {
	for (final Annotation ad : annotations) {
	    final Class<? extends Annotation> thisType = ad.annotationType();
	    if (thisType == annotationType) {
		return ad;
	    }
	}
	return null;
    }

    /**
     * Add the specified annotation to the list of property annotations.
     *
     * @param annotation
     * @return
     */
    public NewProperty addAnnotation(final Annotation annotation) {
	if (!containsAnnotationDescriptorFor(annotation.annotationType())) {
	    annotations.add(annotation);
	}
	return this;
    }
}
