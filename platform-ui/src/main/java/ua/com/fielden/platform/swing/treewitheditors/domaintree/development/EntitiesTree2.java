package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTree2;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeCellEditor2;

/**
 * A tree of entities with their properties.
 *
 * @author TG Team
 *
 */
public class EntitiesTree2<DTM extends IDomainTreeManager> extends MultipleCheckboxTree2 {
    private static final long serialVersionUID = -8348899877560659870L;

    private final EntitiesTreeModel2<DTM> entitiesModel;

    /**
     * Creates entities tree and provides : filtering, toolTips and node's enablement.
     *
     * @param entitiesTreeModel
     *            - the tree model to be used in EntitiesTree.
     */
    public EntitiesTree2(final EntitiesTreeModel2<DTM> entitiesTreeModel2) {
	super(entitiesTreeModel2);

	this.entitiesModel = entitiesTreeModel2;
	// add filter listener to expand tree after filtering
	this.entitiesModel.getFilterableModel().addFilterListener(new IFilterListener() {
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


	setModel(entitiesModel.getFilterableModel());

	// TODO during expanding "warming up" action has adequate performance. But after that... UI expanding is very slow! Please, investigate.
	addTreeWillExpandListener(this.entitiesModel.createTreeWillExpandListener());

	// checking strategies and synchronization with blocks.
	getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	setCellRenderer(new FilterableEntitiesTreeCellRenderer(entitiesTreeModel2));
	setCellEditor(new MultipleCheckboxTreeCellEditor2(this, new FilterableEntitiesTreeCellRenderer(entitiesTreeModel2)));

	setRootVisible(false);
	expandRow(0);
    }

    public EntitiesTreeModel2<DTM> getEntitiesModel() {
	return entitiesModel;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean canExpand(final Object lastPathComponent) {
	final EntitiesTreeNode2<DTM> node = (EntitiesTreeNode2<DTM>) lastPathComponent;
	if(node.getChildCount() == 1 && node.getFirstChild() != null){
	    final EntitiesTreeNode2<DTM> childNode = (EntitiesTreeNode2<DTM>) node.getFirstChild();
	    final String propertyName = childNode.getUserObject().getValue();
	    return !AbstractDomainTree.isDummyMarker(propertyName);
	}
	return  true;
    }
}
