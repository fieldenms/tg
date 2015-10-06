package ua.com.fielden.platform.dom;

/**
 * Represents simple HTML tag's attribute that consists of name only. Useful for boolean attributes.
 *
 * @author TG Team
 *
 */
public class NoValueAttribute extends Attribute<Object> {

    public NoValueAttribute(final String name) {
        super(name, null, null);
    }
}
