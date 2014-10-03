package ua.com.fielden.platform.dom;

import java.util.ArrayList;

public class ClassAttribute extends CollectionalAttribute<String> {

    public ClassAttribute() {
	super("class", new ArrayList<String>(), true, "=", " ");
    }

    @Override
    public ClassAttribute addValue(final String attrValue) {
        return (ClassAttribute)super.addValue(attrValue);
    }

    @Override
    public ClassAttribute removeValue(final String name) {
        return (ClassAttribute)super.removeValue(name);
    }

    @Override
    public ClassAttribute values(final String... values) {
        return (ClassAttribute)super.values(values);
    }
}
