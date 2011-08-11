package ua.com.fielden.platform.swing.treetable;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

import ua.com.fielden.platform.swing.menu.filter.AbstractFilterableTreeModel;

/**
 * Class for {@link JXTreeTable} filtering
 * 
 * @author oleh
 * 
 */
public class FilterableTreeTableModel extends AbstractFilterableTreeModel implements TreeTableModel {

    private boolean reloading = false;

    /**
     * creates new {@link FilterableTreeTableModel} with specified original {@link DynamicTreeTableModel}
     * 
     * @param model
     */
    public FilterableTreeTableModel(final DynamicTreeTableModel model) {
	super(model);
    }

    /**
     * Creates new instance of {@link FilterableTreeTableModel} with specified {@link DynamicTreeTableModel} and with the andMode see {@link #isAndMode()} for more information
     * 
     * @param model
     * @param andMode
     */
    public FilterableTreeTableModel(final DynamicTreeTableModel model, final boolean andMode) {
	super(model, andMode);
    }

    @Override
    public void reload() {
	reloading = true;
	getOriginModel().reload();
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		reloading = false;
	    }

	});
    }

    @Override
    public Class<?> getColumnClass(final int arg0) {
	return getOriginModel().getColumnClass(arg0);
    }

    @Override
    public int getColumnCount() {
	return getOriginModel().getColumnCount();
    }

    @Override
    public String getColumnName(final int arg0) {
	return getOriginModel().getColumnName(arg0);
    }

    @Override
    public int getHierarchicalColumn() {
	return getOriginModel().getHierarchicalColumn();
    }

    @Override
    public Object getValueAt(final Object arg0, final int arg1) {
	return getOriginModel().getValueAt(arg0, arg1);
    }

    @Override
    public boolean isCellEditable(final Object arg0, final int arg1) {
	return getOriginModel().isCellEditable(arg0, arg1);
    }

    @Override
    public void setValueAt(final Object arg0, final Object arg1, final int arg2) {
	final TreeTableNode ttn = (TreeTableNode) arg1;

	if (arg2 < ttn.getColumnCount()) {
	    ttn.setValueAt(arg0, arg2);
	}

    }

    @Override
    public DynamicTreeTableModel getOriginModel() {
	return (DynamicTreeTableModel) super.getOriginModel();
    }

    /**
     * Returns value that indicates whether the model is reloading after the filtering or not
     * 
     * @return
     */
    public boolean isReloading() {
	return reloading;
    }
}
