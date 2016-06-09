package ua.com.fielden.platform.dom;

/**
 * Represents simple HTML tag's attribute that consists of name, value and an assignment between them (e.g. <code>id="idValue"</code>)
 *
 * @author TG Team
 *
 */
public class SingleValueAttribute extends Attribute<Object> {

    public SingleValueAttribute(final String name, final Object value) {
        super(name, value, "=");
    }
}
