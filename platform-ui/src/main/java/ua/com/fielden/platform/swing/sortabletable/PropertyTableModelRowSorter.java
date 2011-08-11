package ua.com.fielden.platform.swing.sortabletable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.RowSorter;
import javax.swing.SortOrder;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;

/**
 * {@link RowSorter} that supports multiple sorting (Implemented for the {@link PropertyTableModel})
 * 
 * @author oleh
 * 
 * @param <T>
 */
public class PropertyTableModelRowSorter<T extends AbstractEntity> extends RowSorter<PropertyTableModel<T>> {

    /**
     * The sort keys.
     */
    private List<SortKey> sortKeys;

    /**
     * Whether or not the specified column is sortable, by column.
     */
    private boolean[] isSortable;

    /**
     * {@link PropertyTableModel} to which this {@link RowSorter} is applied
     */
    private final PropertyTableModel<T> model;

    /**
     * Creates {@link PropertyTableModelRowSorter} for the specified {@link PropertyTableModel}
     * 
     * @param model
     */
    public PropertyTableModelRowSorter(final PropertyTableModel<T> model) {
	this.model = model;
	this.isSortable = null;
	sortKeys = new ArrayList<SortKey>();
    }

    @Override
    public void allRowsChanged() {
    }

    @Override
    public int convertRowIndexToModel(final int index) {
	return index;
    }

    @Override
    public int convertRowIndexToView(final int index) {
	return index;
    }

    @Override
    public PropertyTableModel<T> getModel() {
	return model;
    }

    @Override
    public int getModelRowCount() {
	return model.getRowCount();
    }

    @Override
    public List<? extends SortKey> getSortKeys() {
	return new ArrayList<SortKey>(sortKeys);
    }

    @Override
    public int getViewRowCount() {
	return model.getRowCount();
    }

    @Override
    public void modelStructureChanged() {
	isSortable = null;
	sortKeys.clear();
    }

    @Override
    public void rowsDeleted(final int firstRow, final int endRow) {

    }

    @Override
    public void rowsInserted(final int firstRow, final int endRow) {

    }

    @Override
    public void rowsUpdated(final int firstRow, final int endRow) {

    }

    @Override
    public void rowsUpdated(final int firstRow, final int endRow, final int column) {

    }

    @Override
    public void setSortKeys(final List<? extends SortKey> keys) {
	final List<SortKey> old = this.sortKeys;
	if (keys != null && keys.size() > 0) {
	    final int max = model.getColumnCount();
	    final Iterator<? extends SortKey> iterator = keys.iterator();
	    while (iterator.hasNext()) {
		final SortKey key = iterator.next();
		if (key == null || key.getColumn() < 0 || key.getColumn() >= max || key.getSortOrder().equals(SortOrder.UNSORTED) || !isSortable(key.getColumn())) {
		    iterator.remove();
		}
	    }
	    this.sortKeys = Collections.unmodifiableList(new ArrayList<SortKey>(keys));
	} else {
	    this.sortKeys = Collections.emptyList();
	}
	if (!this.sortKeys.equals(old)) {
	    fireSortOrderChanged();
	}
    }

    @Override
    public void toggleSortOrder(final int column) {
    }

    /**
     * Toggles {@link SortKey} instance that is on the specified column
     * 
     * @param column
     * @param discardPrevious
     */
    public void toggleSortOrder(final int column, final boolean discardPrevious) {
	checkColumn(column);
	if (isSortable(column)) {
	    final List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
	    SortKey sortKey;
	    final int sortIndex = getSortOrderForColumnIndex(column);
	    if (discardPrevious) {
		if (sortIndex < 0) {
		    keys.clear();
		    keys.add(new SortKey(column, SortOrder.ASCENDING));
		} else {
		    sortKey = toggle(keys.get(sortIndex));
		    keys.clear();
		    if (sortKey != null) {
			keys.add(sortKey);
		    }
		}
	    } else {
		if (sortIndex < 0) {
		    keys.add(new SortKey(column, SortOrder.ASCENDING));
		} else {
		    sortKey = toggle(keys.get(sortIndex));
		    if (sortKey == null) {
			keys.remove(sortIndex);
		    } else {
			keys.set(sortIndex, sortKey);
		    }
		}
	    }
	    setSortKeys(keys);
	}
    }

    /**
     * Toggles specified {@link SortKey} instance
     * 
     * @param sortKey
     * @return
     */
    private SortKey toggle(final SortKey sortKey) {
	if (sortKey.getSortOrder() == SortOrder.ASCENDING) {
	    return new SortKey(sortKey.getColumn(), SortOrder.DESCENDING);
	}
	return null;
    }

    /**
     * 
     * 
     * @param column
     * @param sortable
     */
    public void setSortable(final int column, final boolean sortable) {
	checkColumn(column);
	if (isSortable == null) {
	    isSortable = new boolean[model.getColumnCount()];
	    for (int i = isSortable.length - 1; i >= 0; i--) {
		isSortable[i] = true;
	    }
	}
	isSortable[column] = sortable;
	if (!sortable) {
	    setSortKeys(new ArrayList<SortKey>(sortKeys));
	}
    }

    public void setSortable(final String columnKey, final boolean sortable) {
	final int columnIndex = model.getColumnForName(columnKey);
	if (columnIndex < 0) {
	    return;
	}
	setSortable(columnIndex, sortable);
    }

    private void checkColumn(final int column) {
	if (column < 0 || column >= model.getColumnCount()) {
	    throw new IndexOutOfBoundsException("column beyond range of TableModel");
	}
    }

    /**
     * 
     * @param column
     * @return
     */
    public boolean isSortable(final int column) {
	checkColumn(column);
	return (isSortable == null) ? true : isSortable[column];
    }

    /**
     * Set new {@link SortOrder} for the {@link SortKey} specified with {@code key}. If the {@code discardPrevious} is true then sortKeys list will be cleared first<br>
     * <ul>
     * <li>
     * In order to modify existing {@link SortKey} please set the {@code discardPrevious} parameter to false.</li>
     * <li>
     * If the appropriate {@link SortKey} instance doesn't exists then the new one will be add</li>
     * <li>
     * If the {@code discardPrevious} is set to true then all previous {@link SortKey}s will be remove and the new one will be create</li>
     * </ul>
     * PLEASE NOTE: The column associated with {@code key} parameter must be sortable <br>
     * <br>
     * 
     * @param sortOrder
     * @param key
     * @param discardPrevious
     */
    public void setOrder(final SortOrder sortOrder, final String key, final boolean discardPrevious) {
	final int columnIndex = model.getColumnForName(key);
	if (columnIndex < 0 || !isSortable(columnIndex)) {
	    return;
	}
	final List<SortKey> keys = new ArrayList<SortKey>(sortKeys);
	if (discardPrevious) {
	    keys.clear();
	    if (!sortOrder.equals(SortOrder.UNSORTED)) {
		keys.add(new SortKey(columnIndex, sortOrder));
	    }
	} else {
	    final int sortIndex = getSortOrderForColumnIndex(columnIndex);
	    if (sortIndex < 0) {
		if (!sortOrder.equals(SortOrder.UNSORTED)) {
		    keys.add(new SortKey(columnIndex, sortOrder));
		}
	    } else {
		if (!sortOrder.equals(SortOrder.UNSORTED)) {
		    keys.set(sortIndex, new SortKey(columnIndex, sortOrder));
		} else {
		    keys.remove(sortIndex);
		}
	    }
	}
	setSortKeys(keys);
    }

    private int getSortOrderForColumnIndex(final int columnIndex) {
	for (int sortIndex = sortKeys.size() - 1; sortIndex >= 0; sortIndex--) {
	    if (sortKeys.get(sortIndex).getColumn() == columnIndex) {
		return sortIndex;
	    }
	}
	return -1;
    }

    /**
     * Returns value that indicates whether the column associated with key is sortable or not sortable
     * 
     * @param key
     * @return
     */
    public boolean isSortable(final String key) {
	final int columnIndex = model.getColumnForName(key);
	if (columnIndex < 0) {
	    return false;
	}
	if (isSortable(columnIndex)) {
	    return true;
	}
	return false;
    }

    /**
     * Initiates this {@link RowSorter} with given {@link SortKey}s and array of sortable columns
     * 
     * @param mappings
     * @param sortKeys
     * @param sortable
     */
    public void initOrderingWith(final List<? extends AbstractPropertyColumnMapping<T>> mappings, final List<SortKey> sortKeys, final boolean[] sortable) {
	if (mappings == null) {
	    return;
	}
	if (sortable != null) {
	    for (int counter = 0; counter < sortable.length; counter++) {
		final boolean sortableItem = sortable[counter];
		final String key = mappings.get(counter).getPropertyName();
		setSortable(key, sortableItem);
	    }
	}
	if (sortKeys != null) {
	    final List<SortKey> keys = new ArrayList<SortKey>();
	    setSortKeys(keys);
	    for (final SortKey key : sortKeys) {
		final String itemKey = mappings.get(key.getColumn()).getPropertyName();
		setOrder(key.getSortOrder(), itemKey, false);
	    }
	}
    }

    public SortOrder getSortOrder(final String key) {
	final int columnIndex = model.getColumnForName(key);
	if (columnIndex < 0) {
	    return null;
	}
	final int sortIndex = getSortOrderForColumnIndex(columnIndex);
	return isSortable(columnIndex) ? (sortIndex < 0 ? SortOrder.UNSORTED : getSortKeys().get(sortIndex).getSortOrder()) : null;
    }

    public int getOrder(final String key) {
	final int columnIndex = model.getColumnForName(key);
	if (columnIndex < 0) {
	    return -1;
	}
	return getSortOrderForColumnIndex(columnIndex);
    }

    /**
     * Returns the array that indicates what columns are sortable and which are not sortable
     * 
     * @return
     */
    public boolean[] getIsSortable() {
	if (isSortable == null) {
	    return null;
	}
	return Arrays.copyOf(isSortable, isSortable.length);
    }
}
