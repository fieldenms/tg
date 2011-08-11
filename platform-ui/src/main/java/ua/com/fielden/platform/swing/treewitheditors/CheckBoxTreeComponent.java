package ua.com.fielden.platform.swing.treewitheditors;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateButtonModel.State;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateCheckbox;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

/**
 * Represents the check box with four different states (see {@link State} for more information about check box states) that will be placed on the tree item. This implementation of
 * the {@link ITreeCheckingModelComponent} must be associated with {@link DefaultTreeCheckingModel}
 * 
 * @author oleh
 * 
 */
public class CheckBoxTreeComponent implements ITreeCheckingModelComponent {

    private final QuadristateCheckbox checkBox;

    private final MultipleCheckboxTree checkBoxtree;

    private final int index;

    /**
     * Creates new {@link CheckBoxTreeComponent} with {@link QuadristateCheckbox} and associated {@link DefaultTreeCheckingModel}
     * 
     * @param treeCheckingModel
     */
    public CheckBoxTreeComponent(final MultipleCheckboxTree checkBoxtree, final int index) {
	this.checkBox = new QuadristateCheckbox();
	this.checkBox.setBackground(UIManager.getColor("Tree.textBackground"));
	this.checkBoxtree = checkBoxtree;
	this.index = index;
    }

    @Override
    public QuadristateCheckbox getComponent() {
	return checkBox;
    }

    @Override
    public boolean isOnHotspot(final int x, final int y) {
	return getComponent().getBounds().contains(x, y);
    }

    @Override
    public void updateComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	getComponent().setOpaque(false);
	final TreePath path = tree.getPathForRow(row);
	getComponent().setEnabled(checkBoxtree.getCheckingModel(index).isPathEnabled(path) && tree.isEnabled());
	final boolean checked = checkBoxtree.getCheckingModel(index).isPathChecked(path);
	final boolean greyed = checkBoxtree.getCheckingModel(index).isPathGreyed(path);
	if (checked && !greyed) {
	    getComponent().setState(State.CHECKED);
	}
	if (!checked && greyed) {
	    getComponent().setState(State.GREY_UNCHECKED);
	}
	if (checked && greyed) {
	    getComponent().setState(State.GREY_CHECKED);
	}
	if (!checked && !greyed) {
	    getComponent().setState(State.UNCHECKED);
	}

    }

    @Override
    public void actionPerformed(final TreePath treePath) {
	checkBoxtree.getCheckingModel(index).toggleCheckingPath(treePath);
    }
}
