package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeCellEditor2;

public class EntitiesTreeCellEditor extends MultipleCheckboxTreeCellEditor2 {

    private static final long serialVersionUID = -9095582803874450838L;

    public EntitiesTreeCellEditor(final EntitiesTree2 tree, final EntitiesTreeCellRenderer renderer) {
	super(tree, renderer);

	tree.getEntitiesModel().getFilterableModel().addFilterListener(new IFilterListener() {

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
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
	//TODO implemetn logic that determines what buttons should be visible and which not.
	return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @Override
    public Object getCellEditorValue() {
	return ((EntitiesTreeNode2) getTree().getEditingPath().getLastPathComponent()).getUserObject();
    }

    @Override
    public EntitiesTreeCellRenderer getRenderer() {
	return (EntitiesTreeCellRenderer)super.getRenderer();
    }

    @Override
    public boolean isCellEditable(final EventObject event) {
	if(super.isCellEditable(event)){
	    return true;
	}
	if(event instanceof MouseEvent){
	    final MouseEvent e = (MouseEvent) event;
	    if (!e.isControlDown() && !e.isConsumed()) {
		final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		if(path == null){
		    return false;
		}
		final Object lastComponent = path.getLastPathComponent();
		final EntitiesTreeNode2 node = (EntitiesTreeNode2) lastComponent;
		final Class<?> root = node.getUserObject().getKey();
		final String property = node.getUserObject().getValue();
		final Class<?> propertyType = StringUtils.isEmpty(property) ? root : PropertyTypeDeterminator.determineClass(root, property, true, true);
		//TODO finish implementation: determine whether some buttons will be visible or not. If there is visible buttons then the cell is editable othervise it is not
		//		if(AbstractEntity.class.isAssignableFrom(propertyType)){
		//
		//		}
		return false;
	    }
	}
	return false;
    }
}
