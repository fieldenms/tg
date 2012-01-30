package ua.com.fielden.platform.swing.review;

import java.util.Map;

import javax.swing.Action;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.ei.CriteriaInspectorModel;
import ua.com.fielden.platform.swing.ei.DynamicLocatorInspectorModel;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;

import com.jidesoft.grid.TableModelWrapperUtils;

public class DynamicEntityLocatorModel<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends DynamicEntityReviewModel<T, DAO, R> {

    private final IEntitySelectionListener entitySelectionListener;

    private final Action selectAction, cancelAction;

    private boolean useForAutocompleter, searchByDesc, searchByKey;

    public DynamicEntityLocatorModel(//
    final DynamicEntityQueryCriteria<T, DAO> criteria, //
    final PropertyTableModelBuilder<T> builder, //
    final ActionChangerBuilder actionChangerBuilder,//
    final int columns, //
    final Map<String, PropertyPersistentObject> criteriaProperties,//
    final IEntitySelectionListener entitySelectionListener,//
    final Action selectAction,//
    final Action cancelAction,//
    final boolean useForAutcompleter,//
    final boolean searchByKey, final boolean searchByDesc,//
    final Runnable... afterRunActions) {
	super(criteria, builder, null, actionChangerBuilder, columns, criteriaProperties, null, afterRunActions);
	this.entitySelectionListener = entitySelectionListener;
	this.selectAction = selectAction;
	selectAction.setEnabled(false);
	this.cancelAction = cancelAction;
	this.useForAutocompleter = useForAutcompleter;
	this.searchByDesc = searchByDesc;
	this.searchByKey = searchByKey;
	addAfterRunAction(new Runnable() {

	    @Override
	    public void run() {
		DynamicEntityLocatorModel.this.entitySelectionListener.clearSelection();
		getTableModel().getEntityGridInspector().getSelectionModel().clearSelection();
		selectAction.setEnabled(false);
	    }

	});
	setOrder("", SortOrder.ASCENDING, true);
	getTableModel().addTableModelListener(new TableModelListener() {

	    @Override
	    public void tableChanged(final TableModelEvent e) {
		for (final T instance : getTableModel().instances()) {
		    if (entitySelectionListener.isSelected(instance)) {
			final int row = getTableModel().getRowOf(instance);
			final int actualRow = TableModelWrapperUtils.getRowAt(getTableModel().getEntityGridInspector().getModel(), row);
			if (actualRow >= 0) {
			    getTableModel().getEntityGridInspector().getSelectionModel().addSelectionInterval(actualRow, actualRow);
			}
		    }
		}
	    }

	});
    }

    @Override
    protected CriteriaInspectorModel<T, DAO, DynamicEntityQueryCriteria<T, DAO>> createInspectorModel(final DynamicEntityQueryCriteria<T, DAO> criteria) {
	return new DynamicLocatorInspectorModel<T, DAO>(criteria, getEntityMasterFactory());
    }

    ListSelectionListener createEgiSelectionListener() {
	return new ListSelectionListener() {

	    @Override
	    public void valueChanged(final ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    if (e.getFirstIndex() < 0 || e.getLastIndex() < 0) {
			return;
		    }
		    final int rows[] = entitySelectionListener.isMultiselection() ? new int[e.getLastIndex() - e.getFirstIndex() + 1] //
			    : new int[e.getFirstIndex() == e.getLastIndex() ? 1 : 2];
		    if (!entitySelectionListener.isMultiselection()) {
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
		    final int actualRows[] = TableModelWrapperUtils.getActualRowsAt(getTableModel().getEntityGridInspector().getModel(), rows, false);
		    for (int rowIndex = 0; rowIndex < actualRows.length; rowIndex++) {
			final T instance = getTableModel().instance(actualRows[rowIndex]);
			final boolean isSelected = getTableModel().getEntityGridInspector().isRowSelected(rows[rowIndex]);
			if (isMultiselection()) {
			    if (isSelected) {
				entitySelectionListener.performSelection(instance);
			    } else {
				entitySelectionListener.performDeselect(instance);
			    }
			} else {
			    if (isSelected) {
				entitySelectionListener.performSelection(instance);
			    }
			}
		    }
		    if (entitySelectionListener.isSelectionEmpty()) {
			selectAction.setEnabled(false);
		    } else {
			selectAction.setEnabled(true);
		    }
		}
	    }

	};
    }

    @Override
    public DynamicEntityLocator<T, DAO, R> getEntityReview() {
	return (DynamicEntityLocator<T, DAO, R>) super.getEntityReview();
    }

    public Action getCancelAction() {
	return cancelAction;
    }

    public Action getSelectAction() {
	return selectAction;
    }

    @Override
    protected void enableButtons(final boolean enable) {
	super.enableButtons(enable);
	getCancelAction().setEnabled(enable);
	getSelectAction().setEnabled(enable);
    }

    /**
     * Returns value that indicates whether this entity locator allows to chose multiple entities or just one.
     * 
     * @return
     */
    public boolean isMultiselection() {
	return entitySelectionListener.isMultiselection();
    }

    public boolean isUseForAutocompleter() {
	return useForAutocompleter;
    }

    public void setUseForAutocompleter(final boolean useForAutocompleter) {
	this.useForAutocompleter = useForAutocompleter;
    }

    public boolean isSearchByDesc() {
	return searchByDesc;
    }

    public void setSearchByDesc(final boolean searchByDesc) {
	this.searchByDesc = searchByDesc;
    }

    public boolean isSearchByKey() {
	return searchByKey;
    }

    public void setSearchByKey(final boolean searchByKey) {
	this.searchByKey = searchByKey;
    }

}
