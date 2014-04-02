package ua.com.fielden.platform.swing.treewitheditors.development;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A model for {@link MultipleCheckboxTree2}.
 * 
 * @author TG Team
 * 
 */
public class MultipleCheckboxTreeModel2 extends DefaultTreeModel {

    private static final long serialVersionUID = -4494528923282828671L;

    private final List<TreeCheckingModel> checkingModels;
    private final int numOfCheckingModel;

    /**
     * Creates a new tree model for the 'multiple checkbox tree'.
     * 
     * @param manager
     */
    public MultipleCheckboxTreeModel2(final int numOfCheckingModels) {
        super(null);

        checkingModels = new ArrayList<TreeCheckingModel>(numOfCheckingModels);
        this.numOfCheckingModel = numOfCheckingModels;
        for (int modelCounter = 0; modelCounter < numOfCheckingModels; modelCounter++) {
            final DefaultTreeCheckingModel checkingModel = createCheckingModel(modelCounter);
            checkingModels.add(null);
            setCheckingModel(checkingModel, modelCounter);
        }
    }

    /**
     * A {@link DefaultTreeCheckingModel} without losing its checking when {@link #setTreeModel(TreeModel)} invokes.
     * 
     * @author TG Team
     * 
     */
    protected class DefaultTreeCheckingModelWithoutLosingChecking extends DefaultTreeCheckingModel {
        public DefaultTreeCheckingModelWithoutLosingChecking(final TreeModel model) {
            super(model);
        }

        @Override
        public void setTreeModel(final TreeModel newModel) {
            // check out a current "checkings":
            final TreePath[] checkingPaths = this.getCheckingPaths();
            // do actual model setting and lose all "checkings":
            super.setTreeModel(newModel);
            // restore original "checkings":
            this.setCheckingPaths(checkingPaths);
        }
    }

    /**
     * Creates a {@link DefaultTreeCheckingModel} without losing its checking when {@link #setTreeModel(TreeModel)} invokes.
     * 
     * @param index
     * @return
     */
    protected DefaultTreeCheckingModel createCheckingModel(final int index) {
        return new DefaultTreeCheckingModelWithoutLosingChecking(this);
    }

    /**
     * Sets the specified {@link TreeCheckingModel} with new one.
     * 
     * @param newCheckingModel
     */
    private void setCheckingModel(final TreeCheckingModel newCheckingModel, final int index) {
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
                ((DefaultTreeCheckingModel) newCheckingModel).setTreeModel(this);
            }
        }
    }

    /**
     * This action is required to make a view of a tree with this model sensitive for any changes in the model.
     * 
     * @param tree
     */
    public void makeTreeRepaintable(final MultipleCheckboxTree2 tree) {
        for (final TreeCheckingModel checkingModel : checkingModels) {
            // add a treeCheckingListener to repaint upon checking modifications
            checkingModel.addTreeCheckingListener(new TreeCheckingListener() {
                public void valueChanged(final TreeCheckingEvent e) {
                    tree.repaint();
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
}
