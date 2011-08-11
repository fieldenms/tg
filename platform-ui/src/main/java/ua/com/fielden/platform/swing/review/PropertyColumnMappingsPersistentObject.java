package ua.com.fielden.platform.swing.review;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.swing.RowSorter.SortKey;

import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;

/**
 * Allows to persist {@link AbstractPropertyColumnMapping} with their sorting orders as single object.
 * 
 * @author oleh
 * 
 */
public class PropertyColumnMappingsPersistentObject {

    private final List<AbstractPropertyColumnMapping> propertyColumnMappings = new ArrayList<AbstractPropertyColumnMapping>();

    private final List<SortKey> sortKeys;
    private final boolean[] isSortable;

    protected PropertyColumnMappingsPersistentObject() {
	sortKeys = new ArrayList<SortKey>();
	isSortable = new boolean[0];
    }

    /**
     * Initiate {@link PropertyColumnMappingsPersistentObject} with specified list of {@link AbstractPropertyColumnMapping} and sorting orders.
     * 
     * @param propertyColumnMappings
     * @param sortKeys
     * @param isSortable
     */
    public PropertyColumnMappingsPersistentObject(final List<AbstractPropertyColumnMapping> propertyColumnMappings, final List<SortKey> sortKeys, final boolean[] isSortable) {
	this.propertyColumnMappings.addAll(propertyColumnMappings);
	this.sortKeys = sortKeys;
	if (isSortable != null) {
	    this.isSortable = Arrays.copyOf(isSortable, isSortable.length);
	} else {
	    this.isSortable = null;
	}
    }

    /**
     * Returns list of {@link AbstractPropertyColumnMapping}s associated with this {@link PropertyColumnMappingsPersistentObject}.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AbstractPropertyColumnMapping> getPropertyColumnMappings() {
	return Collections.unmodifiableList(propertyColumnMappings);
    }

    /**
     * Returns the list of {@link SortKey}s associated with this {@link PropertyColumnMappingsPersistentObject}.
     * 
     * @return
     */
    public List<SortKey> getSortKeys() {
	if (sortKeys != null) {
	    return Collections.unmodifiableList(sortKeys);
	}
	return null;
    }

    /**
     * Returns the array of sortable column's associated with this {@link PropertyColumnMappingsPersistentObject}.
     * 
     * @return
     */
    public boolean[] getIsSortable() {
	if (isSortable == null) {
	    return null;
	}
	return Arrays.copyOf(isSortable, isSortable.length);
    }

    /**
     * Determines whether this {@link PropertyColumnMappingsPersistentObject} is different then the given one.
     * 
     * @param pObj
     * @return
     */
    public boolean isChanged(final PropertyColumnMappingsPersistentObject pObj) {
	if (this == pObj) {
	    return false;
	}
	if (pObj == null) {
	    return true;
	}
	if (!isPropertyColumnMappingsEqual(pObj)) {
	    return true;
	}
	if ((getSortKeys() == null && getSortKeys() != pObj.getSortKeys()) || (getSortKeys() != null && !getSortKeys().equals(pObj.getSortKeys()))) {
	    return true;
	}
	if (!Arrays.equals(getIsSortable(), pObj.getIsSortable())) {
	    return true;
	}
	return false;
    }

    /**
     * Determines whether property column mappings of this instance are different from the column mappings of the given instance of {@link DynamicCriteriaPersistentObjectUi}.
     * 
     * @param pObj
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean isPropertyColumnMappingsEqual(final PropertyColumnMappingsPersistentObject pObj) {
	final ListIterator<AbstractPropertyColumnMapping> e1 = getPropertyColumnMappings().listIterator();
	final ListIterator<AbstractPropertyColumnMapping> e2 = pObj.getPropertyColumnMappings().listIterator();
	while (e1.hasNext() && e2.hasNext()) {
	    final AbstractPropertyColumnMapping o1 = e1.next();
	    final AbstractPropertyColumnMapping o2 = e2.next();
	    if (!(o1 == null ? o2 == null : isColumnMappingsEqual(o1, o2))) {
		return false;
	    }
	}
	return !(e1.hasNext() || e2.hasNext());
    }

    /**
     * Determines whether two specified {@link AbstractPropertyColumnMapping}s are equal or not.
     * 
     * @param column1
     * @param column2
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean isColumnMappingsEqual(final AbstractPropertyColumnMapping column1, final AbstractPropertyColumnMapping column2) {
	if (column1 == column2) {
	    return true;
	}
	if (!column1.getPropertyName().equals(column2.getPropertyName())) {
	    return false;
	}
	if (!column1.getColumnClass().equals(column2.getColumnClass())) {
	    return false;
	}
	if (!column1.getSize().equals(column2.getSize())) {
	    return false;
	}
	return true;
    }
}
