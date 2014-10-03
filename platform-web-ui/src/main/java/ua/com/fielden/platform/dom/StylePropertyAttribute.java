package ua.com.fielden.platform.dom;

import java.util.ArrayList;

public class StylePropertyAttribute extends CollectionalAttribute<String> {

    public StylePropertyAttribute(final String name) {
	super(name, new ArrayList<>(), false, ":", " ");
    }

    @Override
    public StylePropertyAttribute addValue(final String attrValue) {
        return (StylePropertyAttribute)super.addValue(attrValue);
    }

    @Override
    public StylePropertyAttribute removeValue(final String name) {
        return (StylePropertyAttribute)super.removeValue(name);
    }

    @Override
    public StylePropertyAttribute values(final String... values) {
        return (StylePropertyAttribute)super.values(values);
    }
}
