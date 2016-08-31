package ua.com.fielden.platform.web.layout.api.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.utils.Pair;

/**
 * Represents the layout configuration (i.e. flex layout configuration with classes and styles)
 *
 * @author TG Team
 *
 */
public class FlexLayoutConfig {

    private final Map<String, String> styles;
    private final Set<String> classes;

    FlexLayoutConfig(final Map<String, String> styles, final Set<String> classes) {
        this.styles = styles;
        this.classes = classes;
    }

    /**
     * Renders the layout configuration according to container direction and gap between elements in that container.
     *
     * @param vertical
     * @param gap
     * @return
     */
    String render(final boolean vertical, final int gap) {
        final boolean shouldIncludeGap = gap != 0;
        final Pair<String, String> tempStyle = new Pair<>(vertical ? "margin-bottom" : "margin-right", gap + "px");
        final String classesString = classes.stream().map(clazz -> "\"" + clazz + "\"").collect(Collectors.joining(", "));
        final String styleString = styles.entrySet().stream()
                .filter(entry -> !(shouldIncludeGap && entry.getKey().equals(tempStyle.getKey())))
                .map(entry -> "\"" + entry.getKey() + ":" + entry.getValue() + "\"").collect(Collectors.joining(", "));
        final String gapStyleString = shouldIncludeGap ? "\"" + tempStyle.getKey() + ":" + tempStyle.getValue() + "\"" : "";

        String layout = classesString;
        if (!StringUtils.isEmpty(layout) && !StringUtils.isEmpty(styleString)) {
            layout += ", ";
        }
        layout += styleString;
        if (!StringUtils.isEmpty(layout) && !StringUtils.isEmpty(gapStyleString)) {
            layout += ", ";
        }
        layout += gapStyleString;
        return layout;
    }

    /**
     * Returns value that indicates whether element with this layout configuration is vertical or not. If the layout wasn't configured with direction class then it returns empty
     * value.
     *
     * @return
     */
    Optional<Boolean> isVerticalLayout() {
        if (classes.contains("vertical") || classes.contains("horizontal")) {
            return Optional.of(classes.contains("vertical"));
        }
        return Optional.empty();
    }
}
