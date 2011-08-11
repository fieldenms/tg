package ua.com.fielden.platform.swing.menu;

import javax.swing.JPanel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.filteredtree.FilterableTree;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * A convenient tree implementation targeted at providing a tree-menu consisting of {@link TreeMenuItem} instances.
 * <p>
 * The type parameter specifies the type of the view for the first menu item. This provides an additional convenience as it is quite often the first item is manipulated with
 * programmatically (e.g. activate as a result of some action not directly related to the tree menu).
 *
 * @author TG Team
 */
public class TreeMenu<V extends BaseNotifPanel> extends FilterableTree implements TreeSelectionListener, TreeWillExpandListener {
    private static final long serialVersionUID = 1L;

    private TreePath prevPath = null;
    private final JPanel holder;

    // private final TreeMenuItem<V> firstItem;

    private final BlockingIndefiniteProgressPane blockingPane;

    /**
     * Principal constructor, which provides election model listener implementation handling removal and placement of views from/onto a holder.
     *
     * @param root
     * @param holder
     */
    public TreeMenu(final TreeMenuItem<V> root, final IFilter menuFilter, final JPanel holder, final boolean shouldSelectFirstMenuItem, final boolean rootVisible, final BlockingIndefiniteProgressPane blockingPane) {
	super(root, menuFilter, shouldSelectFirstMenuItem, rootVisible);
	// firstItem = (TreeMenuItem<V>) root.getChildAt(0);
	// initialise blocking pane
	this.blockingPane = blockingPane;

	// add filter listener to expand tree after filtering
	getModel().addFilterListener(new IFilterListener() {
	    private TreePath prevSelected;

	    @Override
	    public void postFilter(final IFilterableModel model) {
	    }

	    @Override
	    public boolean nodeVisibilityChanged(final TreeNode treeNode, final boolean prevValue, final boolean newValue) {
		final TreeMenu<V> menu = TreeMenu.this;
		if (!(newValue && prevValue) && !newValue) {
		    final TreePath nodePath = new TreePath(menu.getModel().getOriginModel().getPathToRoot(treeNode));
		    final TreeMenuItem<?> item = (TreeMenuItem<?>) treeNode;
		    if (!item.isGroupItem() && nodePath.isDescendant(prevSelected) && !canHideNode(item)) {
			item.getView().notify(item.getView().whyCannotClose(), MessageType.WARNING);
			return true;
		    }
		}
		return false;
	    }

	    @Override
	    public void preFilter(final IFilterableModel model) {
		prevSelected = getSelectionPath();
	    }
	});

	this.holder = holder;

	addTreeWillExpandListener(this);
	addTreeSelectionListener(this);
    }

    /**
     * A convenient constructor, which makes the first menu item active.
     *
     * @param root
     * @param menuFilter
     * @param holder
     * @param blockingPane
     */
    public TreeMenu(final TreeMenuItem<V> root, final IFilter menuFilter, final JPanel holder, final BlockingIndefiniteProgressPane blockingPane) {
	this(root, menuFilter, holder, true, false, blockingPane);
    }

    /**
     * Value change event occurs upon a new tree node selection, which corresponds to a new menu selection (user action). This event handler ensures that menu item selection is
     * permitted (i.e. previously selected menu item can become unselected) and initialises holder panel with a view corresponding to the new menu item.
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
	boolean canSelect = true;
	if (e.getOldLeadSelectionPath() != null && e.getNewLeadSelectionPath() != null && prevPath != e.getNewLeadSelectionPath()) {
	    prevPath = e.getOldLeadSelectionPath();

	    final TreeMenuItem<?> item = (TreeMenuItem<?>) e.getOldLeadSelectionPath().getLastPathComponent();
	    if (!item.isGroupItem()) {
		canSelect = canChangePathSelection(item);
		if (!canSelect) { // change is not permitted
		    selectionModel.setSelectionPath(e.getOldLeadSelectionPath());
		    item.getView().notify(item.getView().whyCannotClose(), MessageType.WARNING);
		}
	    }
	}
	if (canSelect && e.getNewLeadSelectionPath() != null) {
	    final TreeMenuItem<?> item = (TreeMenuItem<?>) e.getNewLeadSelectionPath().getLastPathComponent();
	    prevPath = null;
	    activateItem(item);
	}
    }

    /**
     * Returns value that indicates whether specified {@link TreeMenuItem} can be deselect or not.
     *
     * @param item
     * @return
     */
    protected boolean canChangePathSelection(final TreeMenuItem<?> item) {
	return item.getView().canLeave();
    }

    protected boolean canHideNode(final TreeMenuItem<?> item) {
	return item.getView().canLeave();
    }

    /**
     * Provides logic for activating the provided menu item. This logic can be customised in descendants.
     *
     * @param item
     */
    protected void activateItem(final TreeMenuItem<?> item) {
	if (!item.isGroupItem()) {
	    holder.removeAll();
	    holder.add(item.getView());
	    item.getView().getModel().init(blockingPane);
	    holder.invalidate();
	    holder.revalidate();
	    holder.repaint();
	}
    }

    /**
     * Node collapsing leads to selection of a node if the previously selected node is no the path. Thus, need to check whether current node can be unselected.
     */
    @Override
    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
	final Object nodeCollapsing = event.getPath().getLastPathComponent();
	if (getSelectionPath() != null) {
	    for (final Object n : getSelectionPath().getPath()) {
		if (n.equals(nodeCollapsing)) {
		    final TreeMenuItem<?> item = (TreeMenuItem<?>) getSelectionPath().getLastPathComponent();
		    if (!item.isGroupItem() && !canCollapseItem(item)) {
			item.getView().notify(item.getView().whyCannotClose(), MessageType.WARNING);
			throw new ExpandVetoException(event);
		    }
		    break;
		}
	    }
	}
    }

    protected boolean canCollapseItem(final TreeMenuItem<?> item) {
	return item.getView().canLeave();
    }

    @Override
    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
    }

    /**
     * Returns a select menu item or null if nothing is selected.
     *
     * @return
     */
    public TreeMenuItem<?> getSelectedItem() {
	if (getSelectionPath() == null) {
	    return null;
	}
	return getSelectionPath().getLastPathComponent() != null ? (TreeMenuItem<?>) getSelectionPath().getLastPathComponent() : null;
    }

    public JPanel getHolder() {
	return holder;
    }

    public BlockingIndefiniteProgressPane getBlockingPane() {
	return blockingPane;
    }

    /** Provides a convenient access for the first (top) menu item. */
    public TreeMenuItem<V> getFirstItem() {
	return (TreeMenuItem<V>) getModel().getRoot().getChildAt(0);
    }

    @Override
    public boolean shouldCollapse(final TreePath treePath) {
	final TreePath selectedPath = getSelectionPath();
	if (treePath != null && selectedPath != null) {
	    final TreeMenuItem<?> item = (TreeMenuItem<?>) selectedPath.getLastPathComponent();
	    if (!treePath.isDescendant(selectedPath) || item.isGroupItem() || canCollapseItem(item)) {
		return true;
	    }
	    return false;
	} else {
	    return true;
	}
    }

}
