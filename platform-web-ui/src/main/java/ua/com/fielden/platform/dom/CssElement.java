package ua.com.fielden.platform.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class CssElement {

    private final String selector;
    private final Map<String, String> styles = new HashMap<>();

    public CssElement(final String selector) {
        this.selector = selector;
    }

    public CssElement setStyle(final String property, final String value) {
        styles.put(property, value);
        return this;
    }

    @Override
    public String toString() {
        final List<String> styleStrings = generateStylePairs(styles.entrySet());
        return selector + " {"
                + (styleStrings.isEmpty() ? "" : "\n" + StringUtils.join(styleStrings, ";\n") + "\n}\n");
    }

    private List<String> generateStylePairs(final Set<Entry<String, String>> entrySet) {
        final List<String> cssPairs = new ArrayList<>();
        for (final Map.Entry<String, String> entry : entrySet) {
            cssPairs.add(entry.getKey() + ": " + entry.getValue());
        }
        return cssPairs;
    }
}
