package ua.com.fielden.platform.web.layout.api.impl;

import java.util.List;

import ua.com.fielden.platform.web.layout.api.IGap;

public class ContainerCellLayoutConfig extends ContainerConfig implements IGap {

    ContainerCellLayoutConfig(final List<CellConfig> cells) {
        super(cells, 0);
    }

    @Override
    public ContainerConfig withGapBetweenCells(final int pixels) {
        return new ContainerConfig(cells, pixels);
    }

}
