package ua.com.fielden.platform.dom;

import org.apache.commons.lang.StringUtils;

/**
 * Represents the {@link DomElement} that has no children.
 *
 * @author TG Team
 *
 */
public class SingleDomElement extends DomElement {

    /**
     * Creates new {@link SingleDomElement} with specified tag name.
     *
     * @param tagName
     */
    public SingleDomElement(final String tagName) {
	super(tagName);
    }

    /**
     * Throws the {@link UnsupportedOperationException} as far as {@link SingleDomElement} can not have children.
     */
    @Override
    public DomElement add(final DomElement element) {
	throw new UnsupportedOperationException("It is impossible to add an element to single dom element");
    }

    /**
     * Throws the {@link UnsupportedOperationException} as far as {@link SingleDomElement} can not have children.
     */
    @Override
    public DomElement add(final DomElement element, final int index) {
	throw new UnsupportedOperationException("It is impossible to add an element to single dom element");
    }

    /**
     * Throws the {@link UnsupportedOperationException} as far as {@link SingleDomElement} can not have children.
     */
    @Override
    public DomElement remove(final DomElement element) {
	throw new UnsupportedOperationException("It is impossible to remove element from single dom element");
    }

    /**
     * Throws the {@link UnsupportedOperationException} as far as {@link SingleDomElement} can not have children.
     */
    @Override
    public DomElement remove(final int index) {
	throw new UnsupportedOperationException("It is impossible to remove element from single dom element");
    }

    @Override
    public String toString() {
	final String attributes = StringUtils.join(attrs.values(), " ");
	return "<" + tagName + (StringUtils.isEmpty(attributes) ? "" : (" " + attributes)) + ">";
    }
}
