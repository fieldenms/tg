package ua.com.fielden.platform.swing.pivot.analysis.dnd;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragToSupport;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxList;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.PivotAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.PivotTreeTable;

public class PivotListDragToSupport<T extends AbstractEntity<?>> extends AnalysisListDragToSupport<T> {

    private final PivotTreeTable pivotTreeTable;

    public PivotListDragToSupport(final CheckboxList<String> list, final PivotTreeTable pivotTreeTable, final PivotAnalysisModel<T> pivotAnalysisModel) {
	super(list, pivotAnalysisModel.getCriteria().getEntityClass(), pivotAnalysisModel.adtme().getSecondTick());
	this.pivotTreeTable = pivotTreeTable;
    }

    @Override
    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
	final boolean result = super.dropTo(point, what, draggedFrom);
	if (!result) {
	    return false;
	}
	final TreePath selectedPath = pivotTreeTable.getPathForRow(pivotTreeTable.getSelectedRow());
	((AbstractTableModel) pivotTreeTable.getModel()).fireTableStructureChanged();
	pivotTreeTable.getSelectionModel().setSelectionInterval(0, pivotTreeTable.getRowForPath(selectedPath));
	return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CheckboxList<String> getList() {
	return (CheckboxList<String>) super.getList();
    }

}
