package ua.com.fielden.platform.web.layout.api.impl;

import java.util.ArrayList;
import java.util.stream.Collectors;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.api.ILayoutCell;
import ua.com.fielden.platform.web.layout.api.IRepeater;

public class ContainerCellConfig extends ContainerConfig implements ILayoutCell, IRepeater {

    ContainerCellConfig() {
        super(new ArrayList<>(), 0);
    }

    @Override
    public ContainerCellConfig cell(final ContainerConfig container, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(container, layout));
        return this;
    }

    @Override
    public ContainerCellConfig cell(final ContainerConfig container) {
        cells.add(new CellConfig(container));
        return this;
    }

    @Override
    public ContainerCellConfig cell(final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout));
        return this;
    }

    @Override
    public ContainerCellConfig cell() {
        cells.add(new CellConfig());
        return this;
    }

    @Override
    public ContainerCellConfig skip(final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "skip"));
        return this;
    }

    @Override
    public ContainerCellConfig skip() {
        cells.add(new CellConfig("skip"));
        return this;
    }

    @Override
    public ContainerCellConfig select(final String attribute, final String value, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "select:" + attribute + "=" + value));
        return this;
    }

    @Override
    public ContainerCellConfig select(final String attribute, final String value) {
        cells.add(new CellConfig("select:" + attribute + "=" + value));
        return this;
    }

    @Override
    public ContainerCellConfig html(final DomElement dom, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(dom, layout));
        return null;
    }

    @Override
    public ContainerCellConfig html(final DomElement dom) {
        cells.add(new CellConfig(dom));
        return this;
    }

    @Override
    public ContainerCellConfig html(final String html, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "html:" + html));
        return this;
    }

    @Override
    public ContainerCellConfig html(final String html) {
        cells.add(new CellConfig("html:" + html));
        return this;
    }

    @Override
    public ContainerCellConfig subheader(final String title, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "subheader:" + title));
        return this;
    }

    @Override
    public ContainerCellConfig subheader(final String title) {
        cells.add(new CellConfig("subheader:" + title));
        return this;
    }

    @Override
    public ContainerCellConfig subheaderOpen(final String title, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "subheader-open:" + title));
        return this;
    }

    @Override
    public ContainerCellConfig subheaderOpen(final String title) {
        cells.add(new CellConfig("subheader-open:" + title));
        return this;
    }

    @Override
    public ContainerCellConfig subheaderClosed(final String title, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "subheader-closed:" + title));
        return this;
    }

    @Override
    public ContainerCellConfig subheaderClosed(final String title) {
        cells.add(new CellConfig("subheader-closed:" + title));
        return this;
    }

    @Override
    public ContainerCellConfig repeat(final int times) {
        if (cells.isEmpty()) {
            throw new LayoutException("There are no cells to copy.");
        }
        final CellConfig lastCell = cells.get(cells.size() - 1);
        for (int time = 0; time < times - 1; time++) {
            cells.add(lastCell);
        }
        return this;
    }

    @Override
    public ContainerCellLayoutConfig layoutForEach(final FlexLayoutConfig layout) {
        return new ContainerCellLayoutConfig(cells.stream().map(config -> config.setLayoutIfNotPresent(layout)).collect(Collectors.toList()));
    }

    @Override
    public ContainerConfig withGapBetweenCells(final int pixels) {
        return new ContainerConfig(cells, pixels);
    }
}
