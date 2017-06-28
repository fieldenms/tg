package ua.com.fielden.platform.web.layout.api.impl;

import java.util.List;
import java.util.stream.Collectors;

import ua.com.fielden.platform.web.layout.api.IRepeater;

public class ContainerRepeatConfig extends ContainerCellConfig implements IRepeater {

    ContainerRepeatConfig(final List<CellConfig> cells) {
        this.cells.addAll(cells);
    }

    @Override
    public ContainerRepeatConfig repeat(final int times) {
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
