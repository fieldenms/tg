package ua.com.fielden.platform.swing.pivot.analysis.dnd;

import java.awt.Point;

import javax.swing.JComponent;

import ua.com.fielden.platform.swing.categorychart.AnalysisListDragToSupport;
import ua.com.fielden.platform.swing.checkboxlist.CheckboxList;

public class PivotListDragToSupport<T> extends AnalysisListDragToSupport {

    private final IValueSwaper valueSwaper;

    public PivotListDragToSupport(final CheckboxList<T> list, final IValueSwaper valueSwaper) {
	super(list);
	this.valueSwaper = valueSwaper;
    }

    @Override
    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
	final int previousElementIndex = getList().getSelectedValuesInOrder().indexOf(what);
	final boolean result = super.dropTo(point, what, draggedFrom);
	if (!result) {
	    return false;
	}
	final int newElementIndex = getList().getSelectedValuesInOrder().indexOf(what);
	if (newElementIndex == previousElementIndex || previousElementIndex < 0 || newElementIndex < 0) {
	    return true;
	}
	valueSwaper.swapValues(previousElementIndex, newElementIndex);
	return true;
    }

    @Override
    protected CheckboxList<T> getList() {
	return (CheckboxList<T>) super.getList();
    }

}
