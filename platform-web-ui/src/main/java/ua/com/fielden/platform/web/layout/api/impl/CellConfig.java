package ua.com.fielden.platform.web.layout.api.impl;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.layout.api.IFlexContainerLayout;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;

/**
 * Represents layout cell.
 *
 * @author TG Team
 *
 */
public class CellConfig {

    private final Optional<IFlexContainerLayout> optionalContainer;
    private final Optional<String> layoutWidget;

    private Optional<IFlexLayout> optionalLayout = Optional.empty();

    /**
     * Creates empty cell.
     */
    public CellConfig() {
        this(null, null, (String) null);
    }

    /**
     * Creates container cell.
     *
     * @param container
     */
    public CellConfig(final IFlexContainerLayout container) {
        this(container, null, (String) null);
    }

    /**
     * Creates cell with layout container.
     *
     * @param layout
     */
    public CellConfig(final IFlexLayout layout) {
        this(null, layout, (String) null);
    }

    /**
     * Creates container cell with layout configuration.
     *
     * @param container
     * @param layout
     */
    public CellConfig(final IFlexContainerLayout container, final IFlexLayout layout) {
        this(container, layout, (String) null);
    }

    /**
     * Creates the html cell.
     *
     * @param dom
     */
    public CellConfig(final DomElement dom) {
        this(null, null, "html:" + dom.toString());
    }

    /**
     * Creates html cell with layout configuration.
     *
     * @param dom
     * @param layout
     */
    public CellConfig(final DomElement dom, final IFlexLayout layout) {
        this(null, layout, "html:" + dom.toString());
    }

    /**
     * Creates widget cell (e.g. subheader, select or skip cell)
     *
     * @param layoutWidget
     */
    public CellConfig(final String layoutWidget) {
        this(null, null, layoutWidget);
    }

    /**
     * Creates widget cell (e.g. subheader, select or skip cell) with specified layout.
     *
     * @param layout
     * @param layoutWidget
     */
    public CellConfig(final IFlexLayout layout, final String layoutWidget) {
        this(null, layout, layoutWidget);
    }

    /**
     * Set the layout configuration for this cell if it wasn't specified yet.
     *
     * @param layout
     */
    public void setLayoutIfNotPresent(final IFlexLayout layout) {
        if (!optionalLayout.isPresent()) {
            this.optionalLayout = Optional.ofNullable(layout);
        }
    }

    /**
     * Generates cell string for this configuration.
     *
     * @param vertical
     *            - flex-direction of the container in which this cell is placed. The direction calculation is based on layout configuration and default direction.
     * @param isVerticalDefault
     *            - the default flex-direction of the container in which this cell is placed.
     * @param gap
     * @return
     */
    public String render(final boolean vertical, final boolean isVerticalDefault, final int gap) {
        final String widgetString = layoutWidget.map(lw -> "\"" + lw + "\"").orElse("");
        final String gapStyleString = gap == 0 ? "" : "\"" + (vertical ? "margin-bottom" : "margin-right") + ":" + gap + "px\"";
        final String layoutString = optionalLayout.map(layout -> layout.render(vertical, gap)).orElse(gapStyleString);
        final Optional<Boolean> optionalVertical = optionalLayout.flatMap(l -> l.isVerticalLayout());
        final String containerString = optionalContainer.map(c -> c.render(optionalVertical.orElse(!isVerticalDefault), !isVerticalDefault)).orElse("");

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
