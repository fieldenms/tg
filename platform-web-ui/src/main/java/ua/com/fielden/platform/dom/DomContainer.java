package ua.com.fielden.platform.dom;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * It a abstract container for dom elements that renders his children.
 *
 * @author TG Team
 *
 */
public class DomContainer extends DomElement {

    public DomContainer() {
	super(null);
    }

    @Override
    public DomElement id(final String id) {
	throw new UnsupportedOperationException("The Dom container can't have id");
    }

    @Override
    public String id() {
	throw new UnsupportedOperationException("The Dom container can't have id");
    }

    @Override
    public DomElement clazz(final String className, final boolean include) {
	throw new UnsupportedOperationException("The Dom container can't have class attribute specified");
    }

    @Override
    public DomElement clazz(final String... classNames) {
	throw new UnsupportedOperationException("The Dom container can't have class attribute specified");
    }

    @Override
    public String[] getClases() {
	throw new UnsupportedOperationException("The Dom container can't have class attribute specified");
    }

    @Override
    public DomElement style(final String... nameValuePairs) {
	throw new UnsupportedOperationException("The Dom container can't have style attribute specified");
    }


    @Override
    public DomElement mergeStyle(final String... nameValuePairs) {
	throw new UnsupportedOperationException("The Dom container can't have style attribute specified");
    }

    @Override
    public Map<String, List<String>> getStyles() {
	throw new UnsupportedOperationException("The Dom container can't have style attribute specified");
    }

    @Override
    public DomElement attr(final String name, final Object value) {
	throw new UnsupportedOperationException("The Dom container can't have any attributes");
    }


    @Override
    public Attribute<?> getAttr(final String name) {
	throw new UnsupportedOperationException("The Dom container can't have style attribute specified");
    }

    @Override
    public DomElement removeAttr(final String name) {
	throw new UnsupportedOperationException("The Dom container can't have style attribute specified");
    }

    @Override
    public String toString() {
        return StringUtils.join(children, "\n");
    }
}
