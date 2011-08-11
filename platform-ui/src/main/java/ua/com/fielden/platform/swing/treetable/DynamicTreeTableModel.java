package ua.com.fielden.platform.swing.treetable;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

public class DynamicTreeTableModel extends DefaultTreeTableModel {

    /**
     * Reloads the tree model, after the structure of the tree was changed
     */
    public void reload() {
	TreeTableNode treeNode;
	try {
	    treeNode = (TreeTableNode) root;
	} catch (final ClassCastException ex) {
	    return;
	}

	reload(treeNode);
    }

    /**
     * Reloads the subtree where node is the root of the subtree.
     * 
     * @param node
     *            - root of the subtree that must be reloaded
     */
    public void reload(final TreeTableNode node) {
	if (node != null) {
	    fireTreeStructureChanged(this, getPathToRoot(node), null, null);
	}
    }

    // invokes all treeStructureChanged method.
    private void fireTreeStructureChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {

	final TreeModelListener[] listeners = getTreeModelListeners();
	TreeModelEvent treeEvent = null;
	for (int i = listeners.length - 1; i >= 0; i -= 1) {
	    if (treeEvent == null) {
		treeEvent = new TreeModelEvent(source, path, childIndices, children);
	    }
	    (listeners[i]).treeStructureChanged(treeEvent);
	}
    }
}
