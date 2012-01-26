package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.List;

import ua.com.fielden.platform.swing.treetable.DynamicTreeTableModel;

abstract class PivotTreeTableModel extends DynamicTreeTableModel {

    abstract String getColumnTooltipAt(int column);

    abstract List<String> categoryProperties();

    abstract List<String> aggregatedProperties();
}
