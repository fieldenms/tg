package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTree;

/**
 * Defines the order of the {@link TreeCheckingModel}s in the {@link MultipleCheckboxTree}. The first column of check boxes are the items those form the criteria, the second column
 * forms the table headers
 * 
 * @author oleh
 * 
 */
public enum EntitiesTreeColumn {

    CRITERIA_COLUMN(0), TABLE_HEADER_COLUMN(1);

    private final int columnIndex;

    /**
     * Creates new {@link EntitiesTreeColumn} with appropriate column index
     * 
     * @param columnIndex
     */
    private EntitiesTreeColumn(final int columnIndex) {
	this.columnIndex = columnIndex;
    }

    /**
     * Returns the index of the appropriate column.
     * 
     * @return
     */
    public int getColumnIndex() {
	return columnIndex;
    }

    @Override
    public String toString() {
	return getColumnIndex() == 0 ? "Column for criteria properties" : "Table header properties";
    }
}
