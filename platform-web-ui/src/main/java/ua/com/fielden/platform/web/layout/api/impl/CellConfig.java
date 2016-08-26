package ua.com.fielden.platform.web.layout.api.impl;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.layout.api.IFlexContainerLayout;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;

public class CellConfig {

    private final Optional<IFlexContainerLayout> optionalContainer;
    private final Optional<String> layoutWidget;

    private Optional<IFlexLayout> optionalLayout = Optional.empty();

    public CellConfig() {
        this(null, null, (String) null);
    }

    public CellConfig(final IFlexContainerLayout container) {
        this(container, null, (String) null);
    }

    public CellConfig(final IFlexLayout layout) {
        this(null, layout, (String) null);
    }

    public CellConfig(final IFlexContainerLayout container, final IFlexLayout layout) {
        this(container, layout, (String) null);
    }

    public CellConfig(final DomElement dom) {
        this(null, null, "html:" + dom.toString());
    }

    public CellConfig(final DomElement dom, final IFlexLayout layout) {
        this(null, layout, "html:" + dom.toString());
    }

    public CellConfig(final String layoutWidget) {
        this(null, null, layoutWidget);
    }

    public CellConfig(final IFlexLayout layout, final String layoutWidget) {
        this(null, layout, layoutWidget);
    }

    public void setLayoutIfNotPresent(final IFlexLayout layout) {
        if (!optionalLayout.isPresent()) {
            this.optionalLayout = Optional.ofNullable(layout);
        }
    }

    public String render(final boolean vertical, final boolean isVerticalDefault, final int gap) {
        final boolean shouldIncludeGap = gap != 0;
        final Pair<String, String> tempStyle = new Pair<>(vertical ? "margin-bottom" : "margin-right", gap + "px");
        final String gapStyleString = shouldIncludeGap ? "\"" + tempStyle.getKey() + ":" + tempStyle.getValue() + "\"" : "";
        final String widgetString = layoutWidget.isPresent() ? "\"" + layoutWidget.get() + "\"" : "";
        final String layoutString = optionalLayout.isPresent() ? optionalLayout.get().render(vertical, gap) : gapStyleString;
        final Optional<Boolean> optionalVertical = optionalLayout.isPresent() ? optionalLayout.get().isVerticalLayout() : Optional.empty();
        final String containerString = optionalContainer.isPresent() ?
                optionalContainer.get().render(optionalVertical.isPresent() ? optionalVertical.get() : !isVerticalDefault, !isVerticalDefault)
                : "";

        String layout = widgetString;
        if (!StringUtils.isEmpty(layout) && !StringUtils.isEmpty(layoutString)) {
            layout += ", ";
        }
        layout += layoutString;
        if (!StringUtils.isEmpty(layout) && !StringUtils.isEmpty(containerString)) {
            layout += ", ";
        }
        layout += containerString;
        return "[" + layout + "]";
    }

    private CellConfig(final IFlexContainerLayout container, final IFlexLayout flexLayout, final String layoutWidget) {
        this.optionalContainer = Optional.ofNullable(container);
        this.optionalLayout = Optional.ofNullable(flexLayout);
        this.layoutWidget = Optional.ofNullable(layoutWidget);
    }
}
