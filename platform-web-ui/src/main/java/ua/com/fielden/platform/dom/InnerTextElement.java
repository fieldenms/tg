package ua.com.fielden.platform.dom;

import org.apache.commons.lang.StringUtils;

/**
 * A DOM element that represent innerHTML text of any other DOM elements.
 *
 * @author TG Team
 *
 */
public class InnerTextElement extends SingleDomElement {

    /**
     * Represent the inner HTML text.
     */
    private final String text;

    public InnerTextElement(final String text) {
        super(null);
        this.text = text;
    }

    @Override
    public String toString() {
        return StringUtils.isEmpty(text) ? "" : text;
    }
}
