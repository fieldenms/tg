package ua.com.fielden.platform.dom;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Represents the html5 style attribute.
 *
 * @author TG Team
 *
 */
public class StyleAttribute extends Attribute<Map<String, StylePropertyAttribute>> {

    /**
     * Creates new instance of style attribute.
     */
    public StyleAttribute() {
	super("style", new HashMap<>(), "=");
    }

    /**
     * Adds new {@link StylePropertyAttribute} to the value of this style attribute.
     *
     * @param name
     * @param value
     * @return
     */
    public StyleAttribute addStyle(final String name, final String... value) {
	this.value.put(name, new StylePropertyAttribute(name).values(value));
	return this;
    }

    /**
     * Removes the {@link StylePropertyAttribute} from the list of style attribute's values.
     *
     * @param name
     * @return
     */
    public StyleAttribute removeStyle(final String name) {
	this.value.remove(name);
	return this;
    }

    /**
     * Switches on style for specified attribute name.
     *
     * @param name
     * @param values
     * @return
     */
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

    /**
     * Switches off style for specified attribute name.
     *
     * @param name
     * @param values
     * @return
     */
    public StyleAttribute switchOffStyle(final String name, final String... values) {
	final StylePropertyAttribute styleAttr = this.value.get(name);
	if (styleAttr != null) {
	    for (final String value : values) {
		styleAttr.removeValue(value);
	    }
	}
	return this;
    }

    /**
     * Sets value for this style attribute using given name-value pairs (e.g. "border:5px solid red", "width:100%", ...).
     *
     * @param nameValuePairs
     * @return
     */
    public StyleAttribute style(final String... nameValuePairs) {
	this.value.clear();
	return mergeStyle(nameValuePairs);
    }

    /**
     * Merges values for this style attribute values and given name-value pairs (e.g. "border:5px solid red", "width:100%", ...).
     *
     * @param nameValuePairs
     * @return
     */
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

    /**
     * Returns an instance of {@link StylePropertyAttribute} for given attribute name.
     *
     * @param name
     * @return
     */
    public StylePropertyAttribute style(final String name) {
	return this.value.get(name);
    }

    @Override
    public String toString() {
	final String styleStr = StringUtils.join(value.values(), ";");
	return StringUtils.isEmpty(styleStr) ? "" : (name + nameValueSeparator + "\"" + styleStr + "\"");
    }
}
