package ua.com.fielden.platform.dom;

import org.apache.commons.lang.StringUtils;

/**
 * Represents the inner html text.
 *
 * @author TG Team
 *
 */
public class TextElement extends SingleDomElement {

    /**
     * Represent the inner html text.
     */
    private final String text;

    public TextElement(final String text) {
	super(null);
	this.text = text;
    }

    @Override
    public String toString() {
        return StringUtils.isEmpty(text) ? "" : text;
    }
}
