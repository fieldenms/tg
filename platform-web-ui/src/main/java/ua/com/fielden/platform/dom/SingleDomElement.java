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
     * Restricts adding child elements to {@link SingleDomElement}.
     */
    @Override
    public DomElement add(final DomElement... element) {
        throw new UnsupportedOperationException("A single DOM element should not have children.");
    }

    /**
     * Restricts adding child elements to {@link SingleDomElement}.
     */
    @Override
    public DomElement add(final DomElement element, final int index) {
        throw new UnsupportedOperationException("A single DOM element should not have children.");
    }

    /**
     * Restricts removal of child elements from {@link SingleDomElement} as such calls should be recognized as invalid.
     */
    @Override
    public DomElement remove(final DomElement element) {
        throw new UnsupportedOperationException("A single DOM element does not have children.");
    }

    /**
     * Restricts removal of child elements from {@link SingleDomElement} as such calls should be recognized as invalid.
     */
    @Override
    public DomElement remove(final int index) {
        throw new UnsupportedOperationException("A single DOM element does not have children.");
    }

    @Override
    public String toString() {
        final String attributes = StringUtils.join(attrs.values(), " ");
        return "<" + tagName + (StringUtils.isEmpty(attributes) ? "" : (" " + attributes)) + ">";
    }
}
