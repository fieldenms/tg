package ua.com.fielden.platform.web.layout.api.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.layout.api.IAlignment;
import ua.com.fielden.platform.web.layout.api.ICell;
import ua.com.fielden.platform.web.layout.api.IFlex;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;
import ua.com.fielden.platform.web.layout.api.IJustification;
import ua.com.fielden.platform.web.layout.api.ILayoutCellCompleted;
import ua.com.fielden.platform.web.layout.api.ISelect;

public class LayoutCellBuilder implements ICell {

    private final Optional<ContainerConfig> optionalContainer;
    private final Map<String, String> styles = new HashMap<>();
    private final Set<String> classes = new HashSet<>();

    private Optional<Pair<String, String>> optionalSelect = Optional.empty();

    public static ICell layout() {
        return new LayoutCellBuilder(null);
    }

    public static ICell layout(final ContainerConfig containerConfig) {
        return new LayoutCellBuilder(containerConfig);
    }

    private LayoutCellBuilder(final ContainerConfig containerConfig) {
        optionalContainer = Optional.ofNullable(containerConfig);
    }

    @Override
    public IJustification horizontal() {
        classes.add("horizontal");
        return this;
    }

    @Override
    public IJustification vertical() {
        classes.add("vertical");
        return this;
    }

    @Override
    public ICell withStyle(final String style, final String value) {
        styles.put(style, value);
        return this;
    }

    @Override
    public ICell withClass(final String clazz) {
        classes.add(clazz);
        return this;
    }

    @Override
    public IAlignment startJustified() {
        classes.add("start-justified");
        return this;
    }

    @Override
    public IAlignment centerJustified() {
        classes.add("center-justified");
        return this;
    }

    @Override
    public IAlignment endJustified() {
        classes.add("end-justified");
        return this;
    }

    @Override
    public IAlignment justified() {
        classes.add("justified");
        return this;
    }

    @Override
    public IAlignment aroundJustified() {
        classes.add("around-justified");
        return this;
    }

    @Override
    public IFlex startAligned() {
        classes.add("start");
        return this;
    }

    @Override
    public IFlex centerAligned() {
        classes.add("center");
        return this;
    }

    @Override
    public IFlex endAligned() {
        classes.add("end");
        return this;
    }

    @Override
    public ISelect flex() {
        classes.add("flex");
        return this;
    }

    @Override
    public ISelect flex(final int ratio) {
        classes.add("flex-" + ratio);
        return this;
    }

    @Override
    public ISelect flexNone() {
        classes.add("flex-none");
        return this;
    }

    @Override
    public ISelect flexAuto() {
        classes.add("flex-auto");
        return this;
    }

    @Override
    public ILayoutCellCompleted select(final String property, final String value) {
        optionalSelect = Optional.of(new Pair<>(property, value));
        return this;
    }

    @Override
    public IFlexLayout end() {
        return new IFlexLayout() {

            @Override
            public String render(final boolean vertical, final int gap) {
                final boolean shouldIncludeGap = gap != 0;
                final Pair<String, String> tempStyle = new Pair<>(vertical ? "margin-bottom" : "margin-right", gap + "px");
                final String classesString = classes.stream().map(clazz -> "\"" + clazz + "\"").collect(Collectors.joining(", "));
                final String styleString = styles.entrySet().stream()
                        .filter(entry -> !(shouldIncludeGap && entry.getKey().equals(tempStyle.getKey())))
                        .map(entry -> "\"" + entry.getKey() + ":" + entry.getValue() + "\"").collect(Collectors.joining(", "));
                final String gapStyleString = shouldIncludeGap ? "\"" + tempStyle.getKey() + ":" + tempStyle.getValue() + "\"" : "";
                final String selectString = optionalSelect.isPresent() ? "\"select:" + optionalSelect.get().getKey() + "=" + optionalSelect.get().getValue() + "\"" : "";

                String layout = classesString;
                if (!StringUtils.isEmpty(layout) && !StringUtils.isEmpty(styleString)) {
                    layout += ", ";
                }
                layout += styleString;
                if (!StringUtils.isEmpty(layout) && !StringUtils.isEmpty(gapStyleString)) {
                    layout += ", ";
                }
                layout += gapStyleString;
                if (!StringUtils.isEmpty(layout) && !StringUtils.isEmpty(selectString)) {
                    layout += ", ";
                }
                layout += selectString;
                return layout;
            }

            @Override
            public Optional<Boolean> isVerticalLayout() {
                if (classes.contains("vertical") || classes.contains("horizontal")) {
                    return Optional.of(classes.contains("vertical"));
                }
                return Optional.empty();
            }
        };
    }
}
