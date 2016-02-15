package ua.com.fielden.platform.dom;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class CssStyles {

    private final List<CssElement> css = new ArrayList<>();

    public CssStyles add(final CssElement... elements) {
        for (final CssElement element : elements) {
            css.add(element);
        }
        return this;
    }

    @Override
    public String toString() {
        return StringUtils.join(css, "\n");
    }
}
