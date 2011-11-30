package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import javax.swing.tree.TreeNode;

import ua.com.fielden.platform.domaintree.EntitiesTreeNode;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeCellEditor;

public class EntitiesTreeCellEditor extends MultipleCheckboxTreeCellEditor {

    private static final long serialVersionUID = -9095582803874450838L;

    public EntitiesTreeCellEditor(final EntitiesTree tree, final EntitiesTreeCellRenderer renderer) {
	super(tree, renderer);

	tree.getFilterableModel().addFilterListener(new IFilterListener() {

	    @Override
	    public boolean nodeVisibilityChanged(final TreeNode treeNode, final boolean prevValue, final boolean newValue) {
		return false;
	    }

	    @Override
	    public void postFilter(final IFilterableModel model) {

	    }

	    @Override
	    public void preFilter(final IFilterableModel model) {
		fireEditingStopped();
	    }

	});
    }

    @Override
    public Object getCellEditorValue() {
	return ((EntitiesTreeNode) getTree().getEditingPath().getLastPathComponent()).getUserObject();
    }
}
