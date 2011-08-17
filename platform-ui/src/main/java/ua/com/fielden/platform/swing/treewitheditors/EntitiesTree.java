package ua.com.fielden.platform.swing.treewitheditors;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.snappy.SnappyEntitiesTree;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.menu.filter.FilterableTreeModel;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
import ua.com.fielden.platform.treemodel.rules.ITooltipProvider;

/**
 * The tree with entities and sub-properties and 2 check-boxes for each node. Contains features like filtering, toolTips and node's enablement.
 * 
 * @author Jhou
 * 
 */
public class EntitiesTree extends MultipleCheckboxTree{
    private static final long serialVersionUID = 1L;

    private final FilterableTreeModel model;
    private final ITreeItemFilter criteriaColumnFilter, tableHeaderColumnFilter;

    private static ITreeItemFilter createDefaultCriteriaColumnFilter() {
	return new ITreeItemFilter() {

	    @Override
	    public boolean isSatisfies(final DefaultMutableTreeNode treeNode) {
		final TitledObject titledObject = (TitledObject) treeNode.getUserObject();
		if (titledObject.getObject() == null || titledObject.getType() == null || AbstractEntity.class.isAssignableFrom(titledObject.getType())) {
		    return true;
		}
		return false;
	    }

	    @Override
	    public boolean isChildrenSatisfies(final DefaultMutableTreeNode treeNode) {
		return false;
	    }

	};
    }

    private static ITreeItemFilter createDefaultTableHeaderColumnFilter() {
	return new ITreeItemFilter() {

	    @Override
	    public boolean isSatisfies(final DefaultMutableTreeNode treeNode) {
		if (isMarkedAsCritOnly(treeNode)) {
		    return true;
		}

		if (treeNode.getPath().length <= 2) { // disable [Entities] root node and all nodes assigned to "entities".
		    return true;
		}
		return false;
	    }

	    @Override
	    public boolean isChildrenSatisfies(final DefaultMutableTreeNode treeNode) {
		if (isMarkedAsCritOnly(treeNode)) {
		    return true;
		}

		final TitledObject titledObject = (TitledObject) treeNode.getUserObject();

		if (titledObject != null && titledObject.isCollectional()) {
		    return true;
		} else {
		    return false;
		}
	    }

	    private boolean isMarkedAsCritOnly(final DefaultMutableTreeNode treeNode) {
		// TODO verify correctness of this solution
		// disables those props that are annotated as ''CritOnly''
		if (treeNode.getParent() != null && treeNode.getParent().getParent() != null && treeNode.getParent().getParent().getParent() == null) {
		    final TitledObject titledObjectParent = (TitledObject) ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
		    final Class parentClass = titledObjectParent.getType();
		    final TitledObject titledObject = (TitledObject) treeNode.getUserObject();
		    if (titledObject != null && parentClass != null && !"dummy".equals(titledObject.getObject())) {
			try {
			    final Field propertyField = Finder.getFieldByName(parentClass, (String) titledObject.getObject());
			    if (propertyField.isAnnotationPresent(CritOnly.class)) {
				return true;
			    }
			} catch (final Exception e) {
			    System.out.println(e);
			}
		    }
		}
		return false;
	    }
	};
    }

    public EntitiesTree(final EntitiesTreeModel entitiesTreeModel, final String criteriaCaption) {
	this(entitiesTreeModel, criteriaCaption, createDefaultCriteriaColumnFilter(), createDefaultTableHeaderColumnFilter());
    }

    /**
     * Creates entities tree and provides : filtering, toolTips and node's enablement.
     * 
     * @param entitiesTreeModel
     *            - the tree model to be used in EntitiesTree.
     * @param criteriaCaption
     *            - the name of area corresponding to 0-check-box to which properties should be added/removed. (e.g. "selection criteria" for {@link DynamicCriteriaTree} and "rule"
     *            for {@link SnappyEntitiesTree})
     */
    public EntitiesTree(final EntitiesTreeModel entitiesTreeModel, final String criteriaCaption, final ITreeItemFilter criteriaColumnFilter, final ITreeItemFilter tableHeaderColumnFilter) {
	super(2);

	this.criteriaColumnFilter = criteriaColumnFilter;
	this.tableHeaderColumnFilter = tableHeaderColumnFilter;

	setModel(model = createFilteringModel(entitiesTreeModel));

	providePathsEnablement();

	addTreeWillExpandListener(new TreeWillExpandListener() {
	    @Override
	    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
		if (entitiesTreeModel.loadProperties((DefaultMutableTreeNode) event.getPath().getLastPathComponent())) {
		    providePathsEnablement();
		    checkSubtreeFromPath(EntitiesTreeColumn.CRITERIA_COLUMN, event.getPath(), true);
		}
	    }

	    @Override
	    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
	    }
	});

	// checking strategies and synchronization with blocks.
	getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);
	getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).setCheckingMode(CheckingMode.SIMPLE);

	// cell rendering with filtering issues
	final List<ITooltipProvider> toolTipProviders = new ArrayList<ITooltipProvider>();
	toolTipProviders.add(createCriteriaCheckBoxToolTipProvider(criteriaCaption));
	toolTipProviders.add(createResultantCheckBoxToolTipProvider("result set"));
	setCellRenderer(new FilterMultipleCheckboxTreeCellRenderer(this, model, createLabelToolTipProvider(), toolTipProviders));
	setRootVisible(false);
    }

    public EntitiesTreeModel getEntitiesTreeModel() {
	return (EntitiesTreeModel) getModel().getOriginModel();
    }

    /**
     * Extracts toolTip from node's titledObject (if "desc" == true then extracts description otherwise - title).
     * 
     * @param treeNode
     * @param desc
     * @return
     */
    protected String extractToolTipText(final TreeNode treeNode, final boolean desc) {
	final Object userObject = ((DefaultMutableTreeNode) treeNode).getUserObject();
	return ((userObject instanceof TitledObject) ? (desc ? ((TitledObject) userObject).getDesc() : ((TitledObject) userObject).getTitle()) : userObject.toString());
    }

    /**
     * Creates and returns {@link ITooltipProvider} for the renderer's label.
     * 
     * @return
     */
    protected ITooltipProvider createLabelToolTipProvider() {
	return new ITooltipProvider() {

	    @Override
	    public String getToolTip(final TreeNode treeNode) {
		return extractToolTipText(treeNode, true);
	    }

	};
    }

    /**
     * Creates and returns {@link ITooltipProvider} for the first {@link JCheckBox} component, that is responsible for adding property to the criteria or rule.
     * 
     * @param criteriaName
     * @return
     */
    protected ITooltipProvider createCriteriaCheckBoxToolTipProvider(final String criteriaName) {
	return new ITooltipProvider() {

	    @Override
	    public String getToolTip(final TreeNode treeNode) {
		final TreePath path = getPathFor(treeNode);
		if (!getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).isPathEnabled(path)) {
		    return null;
		}
		final TitledObject parentTitle = TitledObject.extractTitleFromTreeNode(getParentNodeFor(treeNode));
		if (parentTitle != null && parentTitle.getType() != null && AbstractUnionEntity.class.isAssignableFrom(parentTitle.getType())) {
		    return "<html>If not selected, then entities with <i><b>" + extractToolTipText(treeNode, false) + "</b></i> will be ignored</html>";
		}
		return criteriaColumnFilter.isSatisfies((DefaultMutableTreeNode) treeNode) ? null : "<html>Add/Remove <b>" + extractToolTipText(treeNode, false) + "</b> to/from "
			+ criteriaName + "</html>"; // no tooltip for disabled node
	    }
	};
    }

    /**
     * Creates and returns {@link ITooltipProvider} for the second {@link JCheckBox} component, that is responsible for adding property to the resultant set of properties.
     * 
     * @param resultantName
     * @return
     */
    protected ITooltipProvider createResultantCheckBoxToolTipProvider(final String resultantName) {
	return new ITooltipProvider() {

	    @Override
	    public String getToolTip(final TreeNode treeNode) {
		final TreePath path = getPathFor(treeNode);
		if (!getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathEnabled(path)) {
		    return null;
		}
		return tableHeaderColumnFilter.isSatisfies((DefaultMutableTreeNode) treeNode) ? null : "<html>Add/Remove <b>" + extractToolTipText(treeNode, false)
			+ "</b> to/from " + resultantName + "</html>"; // no tooltip for disabled node
	    }

	};
    }

    /**
     * Wraps entitiesTreeModel with filtering.
     * 
     * @param entitiesTreeModel
     * @return
     */
    protected FilterableTreeModel createFilteringModel(final EntitiesTreeModel entitiesTreeModel) {
	// wrap the model
	final FilterableTreeModel model = new FilterableTreeModel(entitiesTreeModel);
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

    /**
     * Returns true if the property represented with given tree node is common for {@link AbstractUnionEntity} class, otherwise returns false.
     * 
     * @param treeNode
     * @return
     */
    private boolean isCommonProperty(final TreeNode treeNode) {
	final TreeNode parentNode = getParentNodeFor(treeNode);
	final TitledObject parentTitle = TitledObject.extractTitleFromTreeNode(parentNode);
	final TitledObject grandFatherTtile = TitledObject.extractTitleFromTreeNode(getParentNodeFor(parentNode));
	return "common".equals(parentTitle.getObject()) && parentTitle.getType() == null && grandFatherTtile.getType() != null
	&& AbstractUnionEntity.class.isAssignableFrom(grandFatherTtile.getType());
    }

    /**
     * Returns parent tree node for specified one.
     * 
     * @param treeNode
     * @return
     */
    public TreeNode getParentNodeFor(final TreeNode treeNode) {
	return (treeNode instanceof DefaultMutableTreeNode) ? ((DefaultMutableTreeNode) treeNode).getParent() : null;
    }

    private TreePath getPathFor(final TreeNode treeNode) {
	final LinkedList<TreeNode> nodeList = new LinkedList<TreeNode>();
	TreeNode currentNode = treeNode;
	while (currentNode != null) {
	    nodeList.addFirst(currentNode);
	    currentNode = currentNode.getParent();
	}
	return new TreePath(nodeList.toArray());
    }

    /**
     * Enables/disables all needed nodes.
     */
    public void providePathsEnablement() {
	enablePaths(EntitiesTreeColumn.CRITERIA_COLUMN, false, criteriaColumnFilter);
	enablePaths(EntitiesTreeColumn.TABLE_HEADER_COLUMN, false, tableHeaderColumnFilter);
    }

    /**
     * Set the paths those satisfies the filter to enable. Column identifies the index of the {@link TreeCheckingModel} in the tree.
     * 
     * @param column
     * @param enable
     * @param filter
     */
    protected void enablePaths(final EntitiesTreeColumn column, final boolean enable, final ITreeItemFilter filter) {
	enablePaths(column.getColumnIndex(), enable, filter);
    }

    /**
     * Set the paths those satisfies the filter to be checked. {@code column} identifies the index of the {@link TreeCheckingModel} in the tree.
     * 
     * @param column
     * @param check
     * @param filter
     */
    public void checkUnionPaths(final EntitiesTreeColumn column, final boolean check) {
	checkPaths(column.getColumnIndex(), check, new UnionEntitiesFilter());
    }

    /**
     * Set the paths those satisfies the filter to be checked. Checking process starts with {@code path}. {@code column} identifies the index of the {@link TreeCheckingModel} in
     * the tree.
     * 
     * @param column
     * @param path
     * @param check
     * @param filter
     */
    public void checkSubtreeFromPath(final EntitiesTreeColumn column, final TreePath path, final boolean check) {
	checkSubtreePath(column.getColumnIndex(), path, check, new UnionEntitiesFilter());
    }

    @Override
    public FilterableTreeModel getModel() {
	return model;
    }

    private class UnionEntitiesFilter implements MultipleCheckboxTree.ITreeItemFilter {

	@Override
	public boolean isChildrenSatisfies(final DefaultMutableTreeNode treeNode) {
	    return false;
	}

	@Override
	public boolean isSatisfies(final DefaultMutableTreeNode treeNode) {
	    final TitledObject thisTitle = TitledObject.extractTitleFromTreeNode(treeNode);
	    final TitledObject parentTitle = TitledObject.extractTitleFromTreeNode(EntitiesTree.this.getParentNodeFor(treeNode));
	    if (thisTitle.getType() != null && AbstractEntity.class.isAssignableFrom(thisTitle.getType()) && !AbstractUnionEntity.class.isAssignableFrom(thisTitle.getType())
		    && parentTitle.getType() != null && AbstractUnionEntity.class.isAssignableFrom(parentTitle.getType())) {
		return true;
	    }
	    return false;
	}
    }
}