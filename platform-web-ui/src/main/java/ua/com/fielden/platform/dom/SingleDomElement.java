package ua.com.fielden.platform.dom;

import org.apache.commons.lang.StringUtils;


public class SingleDomElement extends DomElement {

    public SingleDomElement(final String tagName) {
	super(tagName);
    }

    @Override
    public DomElement add(final DomElement element) {
	throw new UnsupportedOperationException("It is impossible to add an element to single dom element");
    }

    @Override
    public DomElement add(final DomElement element, final int index) {
	throw new UnsupportedOperationException("It is impossible to add an element to single dom element");
    }

    @Override
    public DomElement remove(final DomElement element) {
	throw new UnsupportedOperationException("It is impossible to remove element from single dom element");
    }

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
