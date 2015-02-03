package ua.com.fielden.platform.dom;

import java.util.ArrayList;

/**
 * Represents the html5 tag's class attribute.
 *
 * @author TG Team
 *
 */
public class ClassAttribute extends CollectionalAttribute<String> {

    /**
     * Creates new instance of class attribute.
     */
    public ClassAttribute() {
        super("class", new ArrayList<String>(), true, "=", " ");
    }

    /**
     * Adds the class name value to the list of classes.
     */
    @Override
    public ClassAttribute addValue(final String attrValue) {
        return (ClassAttribute) super.addValue(attrValue);
    }

    /**
     * Removes the class name from the list of classes.
     */
    @Override
    public ClassAttribute removeValue(final String name) {
        return (ClassAttribute) super.removeValue(name);
    }

    /**
     * Returns the list classes.
     */
    @Override
    public ClassAttribute values(final String... values) {
        return (ClassAttribute) super.values(values);
    }
}
