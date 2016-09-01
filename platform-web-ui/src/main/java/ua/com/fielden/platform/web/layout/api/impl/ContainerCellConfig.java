package ua.com.fielden.platform.web.layout.api.impl;

import java.util.ArrayList;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.api.ILayoutCell;

public class ContainerCellConfig extends ContainerConfig implements ILayoutCell {

    ContainerCellConfig() {
        super(new ArrayList<>(), 0);
    }

    @Override
    public ContainerRepeatConfig cell(final ContainerConfig container, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(container, layout));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig cell(final ContainerConfig container) {
        cells.add(new CellConfig(container));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig cell(final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig cell() {
        cells.add(new CellConfig());
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig skip(final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "skip"));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig skip() {
        cells.add(new CellConfig("skip"));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig select(final String attribute, final String value, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "select:" + attribute + "=" + value));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig select(final String attribute, final String value) {
        cells.add(new CellConfig("select:" + attribute + "=" + value));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig html(final DomElement dom, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(dom, layout));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig html(final DomElement dom) {
        cells.add(new CellConfig(dom));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig html(final String html, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "html:" + html));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig html(final String html) {
        cells.add(new CellConfig("html:" + html));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig subheader(final String title, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "subheader:" + title));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig subheader(final String title) {
        cells.add(new CellConfig("subheader:" + title));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig subheaderOpen(final String title, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "subheader-open:" + title));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig subheaderOpen(final String title) {
        cells.add(new CellConfig("subheader-open:" + title));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig subheaderClosed(final String title, final FlexLayoutConfig layout) {
        cells.add(new CellConfig(layout, "subheader-closed:" + title));
        return new ContainerRepeatConfig(cells);
    }

    @Override
    public ContainerRepeatConfig subheaderClosed(final String title) {
        cells.add(new CellConfig("subheader-closed:" + title));
        return new ContainerRepeatConfig(cells);
    }
}
