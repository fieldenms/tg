package ua.com.fielden.platform.web.layout.api.impl;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutDirection.HORIZONTAL;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutDirection.UNSPECIFIED;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutDirection.VERTICAL;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the layout configuration (i.e. flex layout configuration with classes and styles)
 *
 * @author TG Team
 *
 */
public class FlexLayoutConfig {

    private final Map<String, String> styles = new LinkedHashMap<>();
    private final Set<String> classes = new LinkedHashSet<>();


    FlexLayoutConfig(final Map<String, String> styles, final Set<String> classes) {
        this.styles.putAll(styles);
        this.classes.addAll(classes);
    }

    /**
     * Renders the layout configuration according to container direction and gap between elements in that container.
     *
     * @param vertical
     * @param gap
     * @return
     */
    String render(final boolean vertical, final int gap) {
        final String classesString = classes.stream().map(clazz -> "\"" + clazz + "\"").collect(Collectors.joining(", "));
        final String styleString = styles.entrySet().stream()
                .filter(entry -> !(gap != 0 && entry.getKey().equals(vertical ? "margin-bottom" : "margin-right")))
                .map(entry -> "\"" + entry.getKey() + ":" + entry.getValue() + "\"").collect(Collectors.joining(", "));
        final String gapStyleString = gap == 0 ? "" : "\"" + (vertical ? "margin-bottom" : "margin-right") + ":" + gap + "px\"";

        return Optional.of(classesString)
        .map(l -> !isEmpty(l) && !isEmpty(styleString) ? l + ", " : l)
        .map(l -> l + styleString)
        .map(l -> !isEmpty(l) && !isEmpty(gapStyleString) ? l + ", " : l)
        .map(l -> l + gapStyleString).get();
    }

    /**
     * Returns value that indicates whether element with this layout configuration is vertical or not. If the layout wasn't configured with direction class then it returns empty
     * value.
     *
     * @return
     */
    LayoutDirection layoutDirection() {
        if (classes.contains("vertical")) {
            return VERTICAL;
        } else if (classes.contains("horizontal")) {
            return HORIZONTAL;
        }
        return UNSPECIFIED;
    }
}
