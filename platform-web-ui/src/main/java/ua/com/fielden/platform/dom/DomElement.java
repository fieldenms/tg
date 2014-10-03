package ua.com.fielden.platform.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class DomElement {

    public final String tagName;

    protected final Map<String, Attribute<?>> attrs = new HashMap<>();

    private final List<DomElement> children = new ArrayList<>();

    private DomElement parent = null;

    public DomElement(final String tagName) {
	this.tagName = tagName;
    }

    public DomElement add(final DomElement element) {
	children.add(element);
	element.parent = this;
	return this;
    }

    public DomElement add(final DomElement element, final int index) {
	children.add(index, element);
	element.parent = this;
	return this;
    }

    public DomElement remove(final DomElement element) {
	children.remove(element);
	element.parent = null;
	return this;
    }

    public DomElement remove(final int index) {
	final DomElement element = children.remove(index);
	element.parent = null;
	return this;
    }

    public DomElement getParent() {
	return parent;
    }

    public DomElement id(final String id) {
	attrs.put("id", new SingleValueAttribute("id", id));
	return this;
    }

    public String id() {
	final Attribute<?> idAttr = attrs.get("id");
	return idAttr == null ? null : idAttr.value.toString();
    }

    public DomElement cLass(final String className, final boolean include) {
	ClassAttribute clazz = (ClassAttribute)attrs.get("class");
	if (clazz == null) {
	    clazz = new ClassAttribute();
	    attrs.put("class", clazz);
	}
	if (include) {
	    clazz.addValue(className);
	} else {
	    clazz.removeValue(className);
	}
	return this;
    }

    public DomElement cLass(final String... classNames) {
    	attrs.put("class", new ClassAttribute().values(classNames));
    	return this;
    }

    public String[] getClases() {
	final ClassAttribute classAttribute = ((ClassAttribute)attrs.get("class"));
	return classAttribute == null ? null : classAttribute.value.toArray(new String[0]);
    }

    public DomElement style(final String... nameValuePairs) {
	getStyle().style(nameValuePairs);
	return this;
    }

    public DomElement mergeStyle(final String... nameValuePairs) {
	getStyle().mergeStyle(nameValuePairs);
	return this;
    }

    public Map<String, List<String>> getStyles() {
	final StyleAttribute style = getStyle();
	final HashMap<String, List<String>> styles = new HashMap<>();
	for(final Map.Entry<String, StylePropertyAttribute> entry : style.value.entrySet()) {
	    styles.put(entry.getKey(), new ArrayList<>(entry.getValue().value));
	}
	return styles;
    }

    private StyleAttribute getStyle() {
	StyleAttribute style = (StyleAttribute)attrs.get("style");
	if (style == null) {
	    style = new StyleAttribute();
	    attrs.put("style", style);
	}
	return style;
    }

    public DomElement attr(final String name, final Object value) {
	switch (name) {
	case "class":
	    cLass(value.toString());
	    break;
	case "style":
	    style(value.toString());
	    break;
	default:
	    attrs.put(name, new SingleValueAttribute(name, value));
	    break;
	}
	return this;
    }

    public Object getAttr(final String name) {
	return attrs.containsKey(name) ? attrs.get(name) : "";
    }

    public DomElement removeAttr(final String name) {
	attrs.remove(name);
	return this;
    }

    @Override
    public String toString() {
	final String attributes = StringUtils.join(attrs.values(), " ");
	return "<" + tagName + (StringUtils.isEmpty(attributes) ? "" : (" " + attributes)) + ">"
		+ (children.isEmpty() ? "" : "\n" + StringUtils.join(children, "\n") + "\n") + "</" + tagName + ">";
    };
}
