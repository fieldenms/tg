package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.domaintree.EntitiesTreeModel2;
import ua.com.fielden.platform.domaintree.EntitiesTreeNode;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.menu.filter.FilterableTreeModel;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTree;
import ua.com.fielden.platform.utils.Pair;

public class EntitiesTree extends MultipleCheckboxTree {

    private static final long serialVersionUID = -8348899877560659870L;

    private final FilterableTreeModel filterableModel;
    private final EntitiesTreeModel2 entitiesModel;

    /**
     * Creates entities tree and provides : filtering, toolTips and node's enablement.
     *
     * @param entitiesTreeModel
     *            - the tree model to be used in EntitiesTree.
     * @param firstTickCaption
     *            - the name of area corresponding to 0-check-box to which properties should be added/removed.
     * @param secondTickCaption
     * 		  - the name of area corresponding to 1-check-box to which properties should be added/removed.
     */
    public EntitiesTree(final EntitiesTreeModel2 entitiesTreeModel2, final String firstTickCaption, final String secondTickCaption) {
	super(2);

	this.entitiesModel = entitiesTreeModel2;
	setModel(filterableModel = createFilteringModel(entitiesTreeModel2));

	new BreadthFirstSearch<Pair<Class<?>, String>, EntitiesTreeNode>().search((EntitiesTreeNode)getEntitiesModel().getRoot(), createTreeTracerPredicate());

	//Added tree checking listeners those listen tree node checking events and add checked property to the appropriate manager.
	addTreeCheckingListener(createTreeCheckingListener(getEntitiesModel().getManager().getFirstTick()), 0);
	addTreeCheckingListener(createTreeCheckingListener(getEntitiesModel().getManager().getSecondTick()), 1);

	// TODO during expanding "warming up" action has adequate performance. But after that... UI expanding is very slow! Please, investigate.
	addTreeWillExpandListener(this.entitiesModel.createTreeWillExpandListener());

	// checking strategies and synchronization with blocks.
	getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);
	getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);

	// cell rendering with filtering issues
	setCellRenderer(new EntitiesTreeCellRenderer(this, firstTickCaption, secondTickCaption));
	final EntitiesTreeCellRenderer renderer = new EntitiesTreeCellRenderer(this, firstTickCaption, secondTickCaption);
	setCellEditor(new EntitiesTreeCellEditor(this, renderer));
	setRootVisible(false);
	expandRow(0);
    }

    protected FilterableTreeModel createFilteringModel(final EntitiesTreeModel2 entitiesTreeModel2) {
	// wrap the model
	final FilterableTreeModel model = new FilterableTreeModel(entitiesTreeModel2);
	// filter by "containing words".
	model.addFilter(new WordFilter());
	// add filter listener to expand tree after filtering
	model.addFilterListener(new IFilterListener() {
	    private TreePath prevSelected;

	    @Override
	    public void preFilter(final IFilterableModel model) {
		prevSelected = getSelectionPath();
	    }

	    @Override
	    public void postFilter(final IFilterableModel model) {
		expandAll();
		setSelectionPath(prevSelected);
	    }

	    @Override
	    public boolean nodeVisibilityChanged(final TreeNode treeNode, final boolean prevValue, final boolean newValue) {
		return false;
	    }
	});
	return model;
    }

    public FilterableTreeModel getFilterableModel() {
	return filterableModel;
    }

    public EntitiesTreeModel2 getEntitiesModel() {
	return entitiesModel;
    }

    @Override
    protected boolean canExpand(final Object lastPathComponent) {
	final EntitiesTreeNode node = (EntitiesTreeNode) lastPathComponent;
	if(node.getChildCount() == 1 && node.getFirstChild() != null){
	    final EntitiesTreeNode childNode = (EntitiesTreeNode) node.getFirstChild();
	    final String propertyName = childNode.getUserObject().getValue();
	    return !AbstractDomainTree.isDummyMarker(propertyName);
	}
	return  true;
    }

    private ITreeNodePredicate<Pair<Class<?>, String>, EntitiesTreeNode> createTreeTracerPredicate(){
	return new ITreeNodePredicate<Pair<Class<?>,String>, EntitiesTreeNode>() {

	    @Override
	    public boolean eval(final EntitiesTreeNode node) {
		final Pair<Class<?>, String> userObject = node.getUserObject();
		final Class<?> root = userObject.getKey();
		final String property = AbstractDomainTree.reflectionProperty(userObject.getValue());
		final TreePath path = new TreePath(getEntitiesModel().getPathToRoot(node));
		if(isRoot(node) || getEntitiesModel().getManager().getRepresentation().getFirstTick().isDisabledImmutably(root, property)){
		    getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).setPathEnabled(path, false);
		}
		if(isRoot(node) || getEntitiesModel().getManager().getRepresentation().getSecondTick().isDisabledImmutably(root, property)){
		    getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).setPathEnabled(path, false);
		}
		if(!isRoot(node) && getEntitiesModel().getManager().getFirstTick().isChecked(root, property)){
		    getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).addCheckingPath(path);
		}
		if(!isRoot(node) && getEntitiesModel().getManager().getSecondTick().isChecked(root, property)){
		    getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).addCheckingPath(path);
		}
		return false;
	    }


	};

    }

    private TreeCheckingListener createTreeCheckingListener(final ITickManager tickManager) {
	return new TreeCheckingListener() {

	    @Override
	    public void valueChanged(final TreeCheckingEvent e) {
		final EntitiesTreeNode node = (EntitiesTreeNode) e.getPath().getLastPathComponent();
		final Pair<Class<?>, String> userObject = node.getUserObject();
		final Class<?> root = userObject.getKey();
		final String property = AbstractDomainTree.reflectionProperty(userObject.getValue());
		if(!isRoot(node)){
		    tickManager.check(root, property, e.isCheckedPath());
		}
	    }
	};
    }

    private boolean isRoot(final EntitiesTreeNode node){
	return EntitiesTreeModel2.ROOT_PROPERTY.equals(node.getUserObject().getValue());
    }
}
