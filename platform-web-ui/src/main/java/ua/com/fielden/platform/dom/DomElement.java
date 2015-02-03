package ua.com.fielden.platform.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Represent the DOM (Domain object model) element.
 *
 * @author TG Team
 *
 */
public class DomElement {
    /**
     * The tag name of the {@link DomElement}.
     */
    public final String tagName;
    /**
     * The map of this {@link DomElement} attributes.
     */
    protected final Map<String, Attribute<?>> attrs = new HashMap<>();
    /**
     * Children of this {@link DomElement} instance.
     */
    protected final List<DomElement> children = new ArrayList<>();
    /**
     * The reference on the parent {@link DomElement}.
     */
    private DomElement parent = null;

    /**
     * Creates new {@link DomElement} instance with specified tag name.
     *
     * @param tagName
     *            - the tag name of this element.
     */
    public DomElement(final String tagName) {
        this.tagName = tagName;
    }

    /**
     * Adds the child {@link DomElement}.
     *
     * @param element
     *            - the element to add to child list.
     * @return
     */
    public DomElement add(final DomElement... elements) {
        for (final DomElement element : elements) {
            children.add(element);
            element.parent = this;
        }
        return this;
    }

    /**
     * Adds the child {@link DomElement} at the specified position.
     *
     * @param element
     *            - the element to add to child list.
     * @param index
     *            - the position at which element should be inserted.
     * @return
     */
    public DomElement add(final DomElement element, final int index) {
        children.add(index, element);
        element.parent = this;
        return this;
    }

    /**
     * Removes the specified element from the child list.
     *
     * @param element
     *            - element to be removed.
     * @return
     */
    public DomElement remove(final DomElement element) {
        children.remove(element);
        element.parent = null;
        return this;
    }

    /**
     * Removes the element at the specified position from the child list.
     *
     * @param index
     *            - position of the element that is to be removed.
     * @return
     */
    public DomElement remove(final int index) {
        final DomElement element = children.remove(index);
        element.parent = null;
        return this;
    }

    /**
     * Returns the number of children in this dom element.
     *
     * @return
     */
    public int childCount() {
	return children.size();
    }

    /**
     * Returns the parent {@link DomElement} element.
     *
     * @return
     */
    public DomElement getParent() {
        return parent;
    }

    /**
     * Set the id for this {@link DomElement}.
     *
     * @param id
     * @return
     */
    public DomElement id(final String id) {
        attrs.put("id", new SingleValueAttribute("id", id));
        return this;
    }

    /**
     * Returns the id for this {@link DomElement}.
     *
     * @return
     */
    public String id() {
        final Attribute<?> idAttr = attrs.get("id");
        return idAttr == null ? null : idAttr.value.toString();
    }

    /**
     * Adds/removes a CSS lass name to/from this {@link DomElement}'s class attribute.
     *
     * @param className
     *            - the class name to add/remove to/from the class attribute.
     * @param include
     *            - indicates whether to add or remove class name value.
     * @return
     */
    public DomElement clazz(final String className, final boolean include) {
        ClassAttribute clazz = (ClassAttribute) attrs.get("class");
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

    /**
     * Adds the specified list of class names to the class attribute.
     *
     * @param classNames
     *            - the class names to add.
     * @return
     */
    public DomElement clazz(final String... classNames) {
        attrs.put("class", new ClassAttribute().values(classNames));
        return this;
    }

    /**
     * Returns the array of class names those are specified in class attribute of this {@link DomElement}.
     *
     * @return
     */
    public String[] getClases() {
        final ClassAttribute classAttribute = ((ClassAttribute) attrs.get("class"));
        return classAttribute == null ? null : classAttribute.value.toArray(new String[0]);
    }

    /**
     * Set the style for this {@link DomElement}.
     *
     * @param nameValuePairs
     * @return
     */
    public DomElement style(final String... nameValuePairs) {
        getStyle().style(nameValuePairs);
        return this;
    }

    /**
     * Merges style attribute values for this {@link DomElement} (see {@link StyleAttribute#mergeStyle(String...)} for more information.)
     *
     * @param nameValuePairs
     *            -- the name - value pairs to be merged.
     * @return
     */
    public DomElement mergeStyle(final String... nameValuePairs) {
        getStyle().mergeStyle(nameValuePairs);
        return this;
    }

    /**
     * Returns map between style name and list of it's values.
     *
     * @return
     */
    public Map<String, List<String>> getStyles() {
        final StyleAttribute style = getStyle();
        final HashMap<String, List<String>> styles = new HashMap<>();
        for (final Map.Entry<String, StylePropertyAttribute> entry : style.value.entrySet()) {
            styles.put(entry.getKey(), new ArrayList<>(entry.getValue().value));
        }
        return styles;
    }

    /**
     * Returns the {@link StyleAttribute} instance for this {@link DomElement}.
     *
     * @return
     */
    private StyleAttribute getStyle() {
        StyleAttribute style = (StyleAttribute) attrs.get("style");
        if (style == null) {
            style = new StyleAttribute();
            attrs.put("style", style);
        }
        return style;
    }

    /**
     * Set the attribute value for this {@link DomElement} instance.
     *
     * @param name
     *            - the attribute name for which value should be set.
     * @param value
     *            - the value to be set for the specifed attribute name.
     * @return
     */
    public DomElement attr(final String name, final Object value) {
        switch (name) {
        case "class":
            clazz(value.toString());
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

    /**
     * Returns the value for the specified attribute name.
     *
     * @param name
     *            - the attribute name for which the appropriate attribute value must be returned.
     * @return
     */
    public Attribute<?> getAttr(final String name) {
	return attrs.containsKey(name) ? attrs.get(name) : null;
    }

    /**
     * Removes the attribute definition from this {@link DomElement} instance.
     *
     * @param name
     * @return
     */
    public DomElement removeAttr(final String name) {
        attrs.remove(name);
        return this;
    }

    @Override
    public String toString() {
        final String attributes = StringUtils.join(attrs.values(), " ");
        return "<" + tagName + (StringUtils.isEmpty(attributes) ? "" : (" " + attributes)) + ">"
                + (children.isEmpty() ? "" : "\n" + StringUtils.join(children, "\n") + "\n")
                + "</" + tagName + ">";
    };
}
