package ua.com.fielden.platform.web.layout.api.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the flex container.
 *
 * @author TG Team
 *
 */
public class ContainerConfig {

    final List<CellConfig> cells = new ArrayList<>();

    final int gap;

    ContainerConfig(final List<CellConfig> cells, final int gap) {
        this.cells.clear();
        this.cells.addAll(cells);
        this.gap = gap;
    }

    String render(final boolean vertical, final boolean isVerticalDefault) {
        String cellsLayout = "";
        for (int cellIndex = 0; cellIndex < cells.size() - 1; cellIndex++) {
            cellsLayout = cellsLayout + cells.get(cellIndex).render(vertical, isVerticalDefault, gap) + ", ";
        }
        cellsLayout += cells.get(cells.size() - 1).render(vertical, isVerticalDefault, 0);
        return cellsLayout;
    }

    @Override
    public String toString() {
        return render(false, false);
    }
}
