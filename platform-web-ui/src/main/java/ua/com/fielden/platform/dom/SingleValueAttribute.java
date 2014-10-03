package ua.com.fielden.platform.dom;

public class SingleValueAttribute extends Attribute<Object> {

    public SingleValueAttribute(final String name, final Object value) {
	super(name, value, "=");
    }
}
