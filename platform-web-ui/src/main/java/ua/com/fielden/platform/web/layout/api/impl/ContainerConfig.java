package ua.com.fielden.platform.web.layout.api.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.api.IFlexContainerLayout;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;
import ua.com.fielden.platform.web.layout.api.IGap;
import ua.com.fielden.platform.web.layout.api.ILayoutCell;

/**
 * Represents the flex container.
 *
 * @author TG Team
 *
 */
public class ContainerConfig implements ILayoutCell {

    private final List<CellConfig> cells = new ArrayList<>();

    private int gap = 0;

    @Override
    public ILayoutCell cell(final IFlexContainerLayout container, final IFlexLayout layout) {
        cells.add(new CellConfig(container, layout));
        return this;
    }

    @Override
    public ILayoutCell cell(final IFlexContainerLayout container) {
        cells.add(new CellConfig(container));
        return this;
    }

    @Override
    public ILayoutCell cell(final IFlexLayout layout) {
        cells.add(new CellConfig(layout));
        return this;
    }

    @Override
    public ILayoutCell cell() {
        cells.add(new CellConfig());
        return this;
    }

    @Override
    public ILayoutCell skip(final IFlexLayout layout) {
        cells.add(new CellConfig(layout, "skip"));
        return this;
    }

    @Override
    public ILayoutCell skip() {
        cells.add(new CellConfig("skip"));
        return this;
    }

    @Override
    public ILayoutCell select(final String attribute, final String value, final IFlexLayout layout) {
        cells.add(new CellConfig(layout, "select:" + attribute + "=" + value));
        return this;
    }

    @Override
    public ILayoutCell select(final String attribute, final String value) {
        cells.add(new CellConfig("select:" + attribute + "=" + value));
        return this;
    }

    @Override
    public ILayoutCell html(final DomElement dom, final IFlexLayout layout) {
        cells.add(new CellConfig(dom, layout));
        return null;
    }

    @Override
    public ILayoutCell html(final DomElement dom) {
        cells.add(new CellConfig(dom));
        return this;
    }

    @Override
    public ILayoutCell subheader(final String title, final IFlexLayout layout) {
        cells.add(new CellConfig(layout, "subheader:" + title));
        return this;
    }

    @Override
    public ILayoutCell subheader(final String title) {
        cells.add(new CellConfig("subheader:" + title));
        return this;
    }

    @Override
    public ILayoutCell subheaderOpen(final String title, final IFlexLayout layout) {
        cells.add(new CellConfig(layout, "subheader-open:" + title));
        return this;
    }

    @Override
    public ILayoutCell subheaderOpen(final String title) {
        cells.add(new CellConfig("subheader-open:" + title));
        return this;
    }

    @Override
    public ILayoutCell subheaderClosed(final String title, final IFlexLayout layout) {
        cells.add(new CellConfig(layout, "subheader-closed:" + title));
        return this;
    }

    @Override
    public ILayoutCell subheaderClosed(final String title) {
        cells.add(new CellConfig("subheader-closed:" + title));
        return this;
    }

    @Override
    public ILayoutCell repeat(final int times) {
        if (cells.isEmpty()) {
            throw new UnsupportedOperationException("There are no cell to copy");
        }
        final CellConfig lastCell = cells.get(cells.size() - 1);
        for (int time = 0; time < times - 1; time++) {
            cells.add(lastCell);
        }
        return this;
    }

    @Override
    public IGap layoutForEach(final IFlexLayout layout) {
        cells.forEach(config -> config.setLayoutIfNotPresent(layout));
        return this;
    }

    @Override
    public IFlexContainerLayout withGapBetweenCells(final int pixels) {
        gap = pixels;
        return this;
    }

    @Override
    public String render(final boolean vertical, final boolean isVerticalDefault) {
        String cellsLayout = "";
        for (int cellIndex = 0; cellIndex < cells.size() - 1; cellIndex++) {
            cellsLayout = cellsLayout + cells.get(cellIndex).render(vertical, isVerticalDefault, gap) + ", ";
        }
        cellsLayout += cells.get(cells.size() - 1).render(vertical, isVerticalDefault, 0);
        return cellsLayout;
    }
}
