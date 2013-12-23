package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationViewForLocator;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityLocator;

import com.jidesoft.grid.TableModelWrapperUtils;

public class GridAnalysisViewForLocator<T extends AbstractEntity<?>> extends GridAnalysisView<T, ILocatorDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = -2079610293855993403L;

    private final List<T> selectedEntities;

    public GridAnalysisViewForLocator(final GridAnalysisModelForLocator<T> model, final GridConfigurationViewForLocator<T> owner) {
	super(model, owner);
	this.selectedEntities = model.getLocatorSelectionModel();
	final EntityGridInspector<T> egi = getEgiPanel().getEgi();
	final ListSelectionListener listener = createEgiSelectionListener(egi, getCentre().getOwner().isMultipleSelection());
	egi.setSelectionMode(getCentre().getOwner().isMultipleSelection() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
	egi.getSelectionModel().addListSelectionListener(listener);
	egi.getColumnModel().getSelectionModel().addListSelectionListener(listener);
	egi.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		if (e.getClickCount() == 2) {
		    final int row = egi.rowAtPoint(e.getPoint());
		    if (row >= 0) {
			getCentre().getSelectAction().actionPerformed(null);
		    }
		}
	    }
	});
    }

    @Override
    public GridConfigurationViewForLocator<T> getOwner() {
        return (GridConfigurationViewForLocator<T>)super.getOwner();
    }

    @Override
    public List<T> getEnhancedSelectedEntities() {
        return selectedEntities == null ? new ArrayList<T>() : Collections.unmodifiableList(new ArrayList<>(selectedEntities));
    }

    @Override
    protected SingleAnalysisEntityLocator<T> getCentre() {
        return getOwner().getOwner();
    }

    private ListSelectionListener createEgiSelectionListener(final EntityGridInspector<T> egi, final boolean isMultipleSelection) {
	return new ListSelectionListener() {

	    @Override
	    public void valueChanged(final ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    if (e.getFirstIndex() < 0 || e.getLastIndex() < 0) {
			return;
		    }
		    final int rows[] = isMultipleSelection ? new int[e.getLastIndex() - e.getFirstIndex() + 1] //
			    : new int[e.getFirstIndex() == e.getLastIndex() ? 1 : 2];
		    if (!isMultipleSelection) {
			if (e.getFirstIndex() == e.getLastIndex()) {
			    rows[0] = e.getFirstIndex();
			} else {
			    rows[0] = e.getFirstIndex();
			    rows[1] = e.getLastIndex();
			}
		    } else {
			for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
			    rows[rowIndex] = e.getFirstIndex() + rowIndex;
			}
		    }
		    final int actualRows[] = TableModelWrapperUtils.getActualRowsAt(egi.getModel(), rows, false);
		    for (int rowIndex = 0; rowIndex < actualRows.length; rowIndex++) {
			final T instance = egi.getActualModel().instance(actualRows[rowIndex]);
			final boolean isSelected = egi.isRowSelected(rows[rowIndex]);
			if (isSelected) {
			    performSelection(instance);
			} else {
			    performDeselect(instance);
			}
		    }
		}
	    }

	    private boolean isSelected(final T entityToCheck) {
		return selectedEntities.contains(entityToCheck);
	    }

	    private void performDeselect(final T selectedObject) {
		selectedEntities.remove(selectedObject);
	    }

	    private void performSelection(final T selectedObject) {
		if (isMultipleSelection) {
		    if (!isSelected(selectedObject)) {
			selectedEntities.add(selectedObject);
		    }
		} else {
		    selectedEntities.clear();
		    selectedEntities.add(selectedObject);
		}
	    }

	};
    }
}
