package ua.com.fielden.platform.web.layout.api.impl;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutDirection.UNSPECIFIED;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutDirection.VERTICAL;

import java.util.Optional;

import ua.com.fielden.platform.dom.DomElement;

/**
 * Represents layout cell.
 *
 * @author TG Team
 *
 */
public class CellConfig {

    private final Optional<ContainerConfig> container;
    private final Optional<String> layoutWidget;
    private final Optional<FlexLayoutConfig> layout;

    /**
     * Creates empty cell.
     */
    public CellConfig() {
        this(Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Creates container cell.
     *
     * @param container
     */
    public CellConfig(final ContainerConfig container) {
        this(Optional.of(container), Optional.empty(), Optional.empty());
    }

    /**
     * Creates cell with layout container.
     *
     * @param layout
     */
    public CellConfig(final FlexLayoutConfig layout) {
        this(Optional.empty(), Optional.of(layout), Optional.empty());
    }

    /**
     * Creates container cell with layout configuration.
     *
     * @param container
     * @param layout
     */
    public CellConfig(final ContainerConfig container, final FlexLayoutConfig layout) {
        this(Optional.of(container), Optional.of(layout), Optional.empty());
    }

    /**
     * Creates the html cell.
     *
     * @param dom
     */
    public CellConfig(final DomElement dom) {
        this(Optional.empty(), Optional.empty(), Optional.of("html:" + dom.toString()));
    }

    /**
     * Creates html cell with layout configuration.
     *
     * @param dom
     * @param layout
     */
    public CellConfig(final DomElement dom, final FlexLayoutConfig layout) {
        this(Optional.empty(), Optional.of(layout), Optional.of("html:" + dom.toString()));
    }

    /**
     * Creates widget cell (e.g. subheader, select or skip cell)
     *
     * @param layoutWidget
     */
    public CellConfig(final String layoutWidget) {
        this(Optional.empty(), Optional.empty(), Optional.of(layoutWidget));
    }

    /**
     * Creates widget cell (e.g. subheader, select or skip cell) with specified layout.
     *
     * @param layout
     * @param layoutWidget
     */
    public CellConfig(final FlexLayoutConfig layout, final String layoutWidget) {
        this(Optional.empty(), Optional.of(layout), Optional.of(layoutWidget));
    }

    /**
     * Set the layout configuration for this cell if it wasn't specified yet.
     *
     * @param layout
     */
    final CellConfig setLayoutIfNotPresent(final FlexLayoutConfig layout) {
        return this.layout.map(l -> this).orElse(new CellConfig(container, Optional.of(layout), layoutWidget));
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
        final String gapStyleString = gap == 0 ? "" : "\"" + (vertical ? "margin-bottom" : "margin-right") + ":" + gap + "px\"";
        final String layoutString = layout.map(layout -> layout.render(vertical, gap)).orElse(gapStyleString);
        final LayoutDirection layoutDirection = layout.map(l -> l.layoutDirection()).orElse(UNSPECIFIED);
        final String containerString = container.
                map(c -> c.render(UNSPECIFIED.equals(layoutDirection) ? !isVerticalDefault : VERTICAL.equals(layoutDirection), !isVerticalDefault)).
                orElse("");

        return Optional.of(layoutWidget.map(lw -> "\"" + lw + "\"").orElse(""))
        .map(l -> !isEmpty(l) && !isEmpty(layoutString) ? l + ", " : l)
        .map(l -> l + layoutString)
        .map(l -> !isEmpty(l) && !isEmpty(containerString) ? l + ", " : l)
        .map(l -> l + containerString)
        .map(l -> "[" + l + "]").get();
    }

    private CellConfig(final Optional<ContainerConfig> container, final Optional<FlexLayoutConfig> flexLayout, final Optional<String> layoutWidget) {
        this.container = container;
        this.layout = flexLayout;
        this.layoutWidget = layoutWidget;
    }
}
