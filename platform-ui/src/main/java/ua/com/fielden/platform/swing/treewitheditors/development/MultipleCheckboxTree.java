package ua.com.fielden.platform.swing.treewitheditors.development;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ua.com.fielden.platform.swing.treewitheditors.Tree;

public class MultipleCheckboxTree extends Tree {

    private static final long serialVersionUID = -239641941602548337L;

    private List<TreeCheckingModel> checkingModels;

    private int numOfCheckingModel;

    /**
     * Creates {@link MultipleCheckboxTree} with default {@link TreeModel} and with specified number of Check boxes
     * 
     * @param numOfCheckingModels
     *            - specified number of check boxes
     */
    public MultipleCheckboxTree(final int numOfCheckingModels) {
	super(getDefaultTreeModel());
	initTree(numOfCheckingModels);
    }

    /**
     * Creates {@link MultipleCheckboxTree} instance with specified {@link TreeModel} and with specified number of Check boxes
     * 
     * @param treemodel
     *            - specified {@link TreeModel}
     * @param numOfCheckingModels
     *            - specified number of check boxes
     */
    public MultipleCheckboxTree(final TreeModel treemodel, final int numOfCheckingModels) {
	super(treemodel);
	initTree(numOfCheckingModels);
    }

    /**
     * Creates {@link MultipleCheckboxTree} with default {@link TreeModel} and with specified number of Check boxes, where the root tree node is the specified one
     * 
     * @param root
     *            - root node of the {@link TreeModel}
     * @param numOfCheckingModels
     *            - specified number of check boxes
     */
    public MultipleCheckboxTree(final TreeNode root, final int numOfCheckingModels) {
	super(root);
	initTree(numOfCheckingModels);
    }

    /**
     * Initiates tree with {@link MultipleCheckboxTreeCellRenderer} and adds the specified number of check boxes to the model
     * 
     * @param numOfCheckingModels
     *            - specified number of check boxes
     */
    private void initTree(final int numOfCheckingModels) {
	ToolTipManager.sharedInstance().registerComponent(this);
	checkingModels = new ArrayList<TreeCheckingModel>(numOfCheckingModels);
	this.numOfCheckingModel = numOfCheckingModels;
	for (int modelCounter = 0; modelCounter < numOfCheckingModels; modelCounter++) {
	    final DefaultTreeCheckingModel checkingModel = new DefaultTreeCheckingModel(getModel());
	    checkingModels.add(null);
	    setCheckingModel(checkingModel, modelCounter);
	}


	setEditable(true);
	setCellRenderer(new MultipleCheckboxTreeCellRenderer(this));
	final MultipleCheckboxTreeCellRenderer renderer = new MultipleCheckboxTreeCellRenderer(this);
	setCellEditor(new MultipleCheckboxTreeCellEditor(this, renderer));

	getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	setShowsRootHandles(true);
	putClientProperty("JTree.lineStyle", "Angled");// for Metal L&F

	final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	final String EDITING_MODE = "Start or stop editing selected path";
	getActionMap().put(EDITING_MODE, createEditingModeAction());
	getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, EDITING_MODE);
    }

    /**
     * Sets the specified {@link TreeCheckingModel} with new one.
     * 
     * @param newCheckingModel
     */
    public void setCheckingModel(final TreeCheckingModel newCheckingModel, final int index) {
	if (checkingModels.contains(newCheckingModel) || (index < 0) || (index >= numOfCheckingModel)) {
	    return;
	}
	final TreeCheckingModel oldCheckingModel = checkingModels.get(index);
	if (oldCheckingModel instanceof DefaultTreeCheckingModel) {
	    ((DefaultTreeCheckingModel) oldCheckingModel).setTreeModel(null);
	}
	if (newCheckingModel != null) {
	    checkingModels.set(index, newCheckingModel);
	    if (newCheckingModel instanceof DefaultTreeCheckingModel) {
		((DefaultTreeCheckingModel) newCheckingModel).setTreeModel(getModel());
	    }
	    // add a treeCheckingListener to repaint upon checking modifications
	    newCheckingModel.addTreeCheckingListener(new TreeCheckingListener() {
		public void valueChanged(final TreeCheckingEvent e) {
		    repaint();
		}
	    });
	}
    }

    /**
     * Returns the index of the specified {@link TreeCheckingModel} instance.
     * 
     * @param checkingModel
     * @return
     */
    public int getCheckingModelIndex(final TreeCheckingModel checkingModel) {
	return checkingModels.indexOf(checkingModel);
    }

    /**
     * Returns the number of {@link TreeCheckingModel}s of this tree
     * 
     * @return
     */
    public int getCheckingModelCount() {
	return numOfCheckingModel;
    }

    /**
     * Returns the {@link TreeCheckingModel} at the specified index
     * 
     * @param index
     * @return
     */
    public TreeCheckingModel getCheckingModel(final int index) {
	return checkingModels.get(index);
    }

    /**
     * Add a path in the checking, for the {@link TreeCheckingModel} at the specified index
     * 
     * @param path
     * @param index
     */
    public void addCheckingPath(final TreePath path, final int index) {
	getCheckingModel(index).addCheckingPath(path);
    }

    /**
     * Add paths in the checking, for the {@link TreeCheckingModel} at the specified index
     * 
     * @param paths
     * @param index
     */
    public void addCheckingPaths(final TreePath[] paths, final int index) {
	getCheckingModel(index).addCheckingPaths(paths);
    }

    /**
     * Adds a listener for <code>TreeChecking</code> events to the {@link TreeCheckingModel} at the specified positin
     * 
     * @param tsl
     *            - the <code>TreeCheckingListener</code> that will be notified when a node is checked
     * 
     * @param index
     *            - specified position of the {@link TreeCheckingModel}
     */
    public void addTreeCheckingListener(final TreeCheckingListener tsl, final int index) {
	getCheckingModel(index).addTreeCheckingListener(tsl);
    }

    /**
     * Clears the checking for the {@link TreeCheckingModel} at the specified position
     * 
     * @param index
     */
    public void clearChecking(final int index) {
	getCheckingModel(index).clearChecking();
    }

    /**
     * Return paths that are in the checking at the specified position.
     * 
     * @param indexs
     */
    public TreePath[] getCheckingPaths(final int index) {
	return getCheckingModel(index).getCheckingPaths();
    }

    /**
     * Returns the paths that are in the checking set and are the (upper) roots of checked trees.
     * 
     * @param index
     *            - position of the {@link TreeCheckingModel} from which checking roots must be returned
     * @return
     */
    public TreePath[] getCheckingRoots(final int index) {
	return getCheckingModel(index).getCheckingRoots();
    }

    /**
     * Returns the paths that are in the greying.
     * 
     * @param index
     *            - position of the {@link TreeCheckingModel} from which graying path must be returned
     * @return
     */
    public TreePath[] getGreyingPaths(final int index) {
	return getCheckingModel(index).getGreyingPaths();
    }

    /**
     * Returns true if the item identified by the path is currently checked for the model specified with index
     * 
     * @param path
     *            - a <code>TreePath</code> identifying a node
     * @param index
     *            - specifies the position of {@link TreeCheckingModel}
     * @return true if the node is checked
     */
    public boolean isPathChecked(final TreePath path, final int index) {
	return getCheckingModel(index).isPathChecked(path);
    }

    /**
     * Remove a path from the {@link TreeCheckingModel} specified with index
     * 
     * @param path
     * @param index
     *            - The index of the {@link TreeCheckingModel} from which the checking path must be removed
     */
    public void removeCheckingPath(final TreePath path, final int index) {
	getCheckingModel(index).removeCheckingPath(path);
    }

    /**
     * Remove paths from the {@link TreeCheckingModel} specified with index.
     * 
     * @param paths
     * @param index
     *            - the index of the {@link TreeCheckingModel} from which checking paths must be removed
     */
    public void removeCheckingPaths(final TreePath[] paths, final int index) {
	getCheckingModel(index).removeCheckingPaths(paths);
    }

    /**
     * Removes a <code>TreeChecking</code> listener from the {@link TreeCheckingModel} specified with index.
     * 
     * @param tsl
     *            the <code>TreeChckingListener</code> to remove
     * @param index
     */
    public void removeTreeCheckingListener(final TreeCheckingListener tsl, final int index) {
	getCheckingModel(index).removeTreeCheckingListener(tsl);
    }

    /**
     * Set path in the checking for the {@link TreeCheckingModel} specified with index
     * 
     * @param path
     * @param index
     *            - the index of the {@link TreeCheckingModel} for which path must be set in the checking
     */
    public void setCheckingPath(final TreePath path, final int index) {
	getCheckingModel(index).setCheckingPath(path);
    }

    /**
     * Set paths that are in the checking for the {@link TreeCheckingModel} specified with index
     * 
     * @param paths
     * @param index
     *            - index of the {@link TreeCheckingModel} for which paths must be set in the checking
     */
    public void setCheckingPaths(final TreePath[] paths, final int index) {
	getCheckingModel(index).setCheckingPaths(paths);
    }

    /**
     * Sets the TreeModel and links it to the existing {@link TreeCheckingModel} s.
     */
    @Override
    public void setModel(final TreeModel newModel) {
	super.setModel(newModel);
	for (int modelCounter = 0; modelCounter < getCheckingModelCount(); modelCounter++) {
	    final TreeCheckingModel checkingModel = getCheckingModel(modelCounter);
	    if (checkingModel != null && checkingModel instanceof DefaultTreeCheckingModel) {
		((DefaultTreeCheckingModel) checkingModel).setTreeModel(newModel);
	    }
	}

    }

    private Action createEditingModeAction() {
	return new AbstractAction() {
	    private static final long serialVersionUID = -5565139442029535686L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (isEditing()) {
		    stopEditing();
		} else {
		    startEditingAtPath(getSelectionPath());
		}
	    }
	};
    }
}
