package ua.com.fielden.platform.web.layout.api.impl;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.web.layout.api.IAlignment;
import ua.com.fielden.platform.web.layout.api.ICell;
import ua.com.fielden.platform.web.layout.api.IFlex;
import ua.com.fielden.platform.web.layout.api.IJustification;
import ua.com.fielden.platform.web.layout.api.ILayoutCellCompleted;

public class LayoutCellBuilder implements ICell {

    private final Map<String, String> styles = new LinkedHashMap<>();
    private final Set<String> classes = new LinkedHashSet<>();

    public static ICell layout() {
        return new LayoutCellBuilder();
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
    public ILayoutCellCompleted flex() {
        classes.add("flex");
        return this;
    }

    @Override
    public ILayoutCellCompleted flex(final int ratio) {
        classes.add("flex-" + ratio);
        return this;
    }

    @Override
    public ILayoutCellCompleted flexNone() {
        classes.add("flex-none");
        return this;
    }

    @Override
    public FlexLayoutConfig end() {
        return new FlexLayoutConfig(styles, classes);
    }
}
