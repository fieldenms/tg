package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.menu.filter.FilterableTreeModel;
import ua.com.fielden.platform.swing.treewitheditors.development.EntitiesTreeModel2;
import ua.com.fielden.platform.swing.treewitheditors.development.EntitiesTreeNode2;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeCellRenderer2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntitiesTreeCellRenderer extends MultipleCheckboxTreeCellRenderer2 {

    private static final long serialVersionUID = 2940223267761789273L;

    private final FilterableTreeModel model;
    private final Font originalFont;
    private final Font derivedFont;
    private final String criteriaName;
    private final String resultSetName;

    public EntitiesTreeCellRenderer(final EntitiesTree2 tree, final String criteriaName, final String resultSetName) {
	super(tree);
	this.model = tree.getFilterableModel();
	this.criteriaName = criteriaName;
	this.resultSetName = resultSetName;
	originalFont = label.getFont();
	derivedFont = originalFont.deriveFont(Font.BOLD);
	label.setLeafIcon(null);
	label.setClosedIcon(null);
	label.setOpenIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	// this action should make matched nodes to render bold.
	if (model.matches((TreeNode) value)) {
	    label.setFont(derivedFont);
	} else {
	    label.setFont(originalFont);
	}

	// "Entity" node distinguishing from property nodes :
	if (((EntitiesTreeNode2) value).getLevel() == 1) {
	    label.setFont(label.getFont().deriveFont(label.getFont().getStyle() + Font.ITALIC));
	}
	return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    @Override
    public EntitiesTree2 getTree() {
	return (EntitiesTree2) super.getTree();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String getLabelToolTipText(final TreePath treePath) {
	if(treePath != null){
	    final Pair<Class<?>, String> rootAndProp = (Pair<Class<?>, String>) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
	    final Class<?> root = rootAndProp.getKey();
	    final String property = rootAndProp.getValue();
	    return EntitiesTreeModel2.extractTitleAndDesc(root, property).getValue();
	}
	return null;
    }

    @Override
    protected String getCheckingComponentToolTipText(final int index, final TreePath treePath) {
	if(treePath != null){
	    final Pair<Class<?>, String> rootAndProp = ((EntitiesTreeNode2) treePath.getLastPathComponent()).getUserObject();
	    final Class<?> root = rootAndProp.getKey();
	    final String property = rootAndProp.getValue();

	    if(index == EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()){
		return createCriteriaCheckboxToolTipText(root, property);
	    }else if(index == EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()){
		return createResultSetCheckboxToolTipText(root, property);
	    }
	    return super.getCheckingComponentToolTipText(index, treePath);
	}
	return null;
    }

    private String createCriteriaCheckboxToolTipText(final Class<?> root, final String property) {
	final IDomainTreeManagerAndEnhancer manager = getTree().getEntitiesModel().getManager();
	if (!EntitiesTreeModel2.ROOT_PROPERTY.equals(property) && !AbstractDomainTree.isCommonBranch(property) && manager.getRepresentation().getFirstTick().isDisabledImmutably(root, AbstractDomainTree.reflectionProperty(property))) { // no tooltip for disabled property
	    return null;
	}
	if (EntityUtils.isUnionEntityType(PropertyTypeDeterminator.transform(root, AbstractDomainTree.reflectionProperty(property)).getKey())) { // parent is union entity
	    return "<html>If not selected, then entities with <i><b>" + EntitiesTreeModel2.extractTitleAndDesc(root, property).getKey() + "</b></i> will be ignored</html>";
	}
	return "<html>Add/Remove <b>" + EntitiesTreeModel2.extractTitleAndDesc(root, property).getKey() + "</b> to/from " + criteriaName + "</html>";
    }

    private String createResultSetCheckboxToolTipText(final Class<?> root, final String property) {
	final IDomainTreeManagerAndEnhancer manager = getTree().getEntitiesModel().getManager();
	if (!EntitiesTreeModel2.ROOT_PROPERTY.equals(property) && !AbstractDomainTree.isCommonBranch(property) && manager.getRepresentation().getSecondTick().isDisabledImmutably(root, AbstractDomainTree.reflectionProperty(property))) { // no tooltip for disabled property
	    return null;
	}
	return "<html>Add/Remove <b>" + EntitiesTreeModel2.extractTitleAndDesc(root, property).getKey() + "</b> to/from " + resultSetName + "</html>";
    }
}
