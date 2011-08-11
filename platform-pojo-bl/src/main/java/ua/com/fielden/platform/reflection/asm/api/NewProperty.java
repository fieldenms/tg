package ua.com.fielden.platform.reflection.asm.api;


/**
 * A convenient abstraction for representing data needed for dynamic construction of properties.
 *
 * @author TG Team
 *
 */
public class NewProperty {
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
}
