package ua.com.fielden.platform.dom;

import java.util.ArrayList;

/**
 * Represents a portion of the HTML <code>style</code> attribute value.
 * It is an descendant of {@link CollectionalAttribute} (e.g. style="...; <b>borer: 5px solid red</b>;...")
 *
 * @author TG Team
 *
 */
public class StylePropertyAttribute extends CollectionalAttribute<String> {

    /**
     * Creates new instance of {@link StylePropertyAttribute} with the specified name, column separator between attribute name and value and space separator between collectional values
     * ((e.g. <b>borer: 5px solid red</b>))
     *
     * @param name
     */
    public StylePropertyAttribute(final String name) {
        super(name, new ArrayList<>(), false, ":", " ");
    }

    /**
     * Adds the attribute value. (e.g. 5px, red -> add "solid" -> 5px, solid, red).
     */
    @Override
    public StylePropertyAttribute addValue(final String attrValue) {
        return (StylePropertyAttribute) super.addValue(attrValue);
    }

    /**
     * Removes the attribute value. (e.g 5px, solid, red -> remove "solid"-> 5px, red).
     */
    @Override
    public StylePropertyAttribute removeValue(final String name) {
        return (StylePropertyAttribute) super.removeValue(name);
    }

    /**
     * Returns the collectional value of the attribute (e.g. 5px, solid, red)
     */
    @Override
    public StylePropertyAttribute values(final String... values) {
        return (StylePropertyAttribute) super.values(values);
    }
}
