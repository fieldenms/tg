package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreePath;


public class ConditionalTreeCellRenderer extends EntitiesTreeCellRenderer {

    private static final long serialVersionUID = -1990891172421167107L;

    public ConditionalTreeCellRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object object, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	final TreeCheckingModel checkingModel = ((CheckboxTree) tree).getCheckingModel();
	final TreePath path = tree.getPathForRow(row);
	this.checkBox.setVisible(checkingModel.isPathEnabled(path) && tree.isEnabled());
	return super.getTreeCellRendererComponent(tree, object, selected, expanded, leaf, row, hasFocus);
    }

}
