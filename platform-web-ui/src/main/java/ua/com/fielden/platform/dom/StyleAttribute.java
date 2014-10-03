package ua.com.fielden.platform.dom;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class StyleAttribute extends Attribute<Map<String, StylePropertyAttribute>> {

    public StyleAttribute() {
	super("style", new HashMap<>(), "=");
    }

    public StyleAttribute addStyle(final String name, final String... value) {
	this.value.put(name, new StylePropertyAttribute(name).values(value));
	return this;
    }

    public StyleAttribute removeStyle(final String name) {
	this.value.remove(name);
	return this;
    }

    public StyleAttribute switchOnStyle(final String name, final String... values) {
	StylePropertyAttribute styleAttr = this.value.get(name);
	if (styleAttr == null) {
	    styleAttr = new StylePropertyAttribute(name);
	    this.value.put(name, styleAttr);
	}
	for (final String value : values) {
	    styleAttr.addValue(value);
	}
	return this;
    }

    public StyleAttribute switchOffStyle(final String name, final String... values) {
	final StylePropertyAttribute styleAttr = this.value.get(name);
	if (styleAttr != null) {
	    for (final String value : values) {
		styleAttr.removeValue(value);
	    }
	}
	return this;
    }

    public StyleAttribute style(final String... nameValuePairs) {
	this.value.clear();
	return mergeStyle(nameValuePairs);
    }

    public StyleAttribute mergeStyle(final String... nameValuePairs) {
	for (final String pair : nameValuePairs) {
	    final String[] nameValue = pair.split(":");
	    if (nameValue.length == 2) {
		addStyle(nameValue[0].trim(), nameValue[1].trim().split("\\s+"));
	    } else {
		throw new IllegalArgumentException("The name value pair must be splitted with \":\"");
	    }
	}
	return this;
    }

    public StylePropertyAttribute style(final String name) {
	return this.value.get(name);
    }

    @Override
    public String toString() {
	final String styleStr = StringUtils.join(value.values(), ";");
	return StringUtils.isEmpty(styleStr) ? "" : (name + nameValueSeparator + "\"" + styleStr + "\"");
    }
}
