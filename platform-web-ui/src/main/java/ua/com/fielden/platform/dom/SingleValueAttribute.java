package ua.com.fielden.platform.dom;

/**
 * Represents simple html5 tag's attribute that consists of name, value and separator between them (e.g. id="idValue")
 *
 * @author TG Team
 *
 */
public class SingleValueAttribute extends Attribute<Object> {

    public SingleValueAttribute(final String name, final Object value) {
	super(name, value, "=");
    }
}
