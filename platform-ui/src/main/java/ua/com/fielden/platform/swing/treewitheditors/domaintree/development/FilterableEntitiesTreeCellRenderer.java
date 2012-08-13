package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeCellRenderer2;

/**
 * {@link MultipleCheckboxTreeCellRenderer2} implementation for filtering.
 *
 * @author TG Team
 *
 */
public class FilterableEntitiesTreeCellRenderer extends MultipleCheckboxTreeCellRenderer2{

    private static final long serialVersionUID = 4633297854388245397L;

    private final Font originalFont;
    private final Font derivedFont;

    /**
     * Initiates this {@link FilterableEntitiesTreeCellRenderer} with appropriate model.
     *
     * @param model
     */
    public FilterableEntitiesTreeCellRenderer(final EntitiesTreeModel2<? extends IDomainTreeManager> model) {
	super(model);

	originalFont = label.getFont();
	derivedFont = originalFont.deriveFont(Font.BOLD);
	label.setLeafIcon(null);
	label.setClosedIcon(null);
	label.setOpenIcon(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntitiesTreeModel2<? extends IDomainTreeManager> getModel() {
	return (EntitiesTreeModel2<? extends IDomainTreeManager>) super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {

	//Configuring the visibility of checking components.
      	setCheckingComponentsVisible(true);

      	final EntitiesTreeNode2<IAbstractAnalysisDomainTreeManager> node = (EntitiesTreeNode2<IAbstractAnalysisDomainTreeManager>) value;
      	//final Class<?> root = node.getUserObject().getKey();
      	final String property = node.getUserObject().getValue();

      	if (!getModel().isNotDummyAndNotCommonProperty(property)) {
      	    setCheckingComponentsVisible(false);
      	}

	/*if (PropertyTypeDeterminator.isDotNotation(property)) {
	    final String parentProperty = PropertyTypeDeterminator.penultAndLast(property).getKey();
	    if (!AbstractDomainTree.isCommonBranch(parentProperty) && EntityUtils.isUnionEntityType(PropertyTypeDeterminator.determinePropertyType(root, AbstractDomainTree.reflectionProperty(parentProperty)))) {
		setCheckingComponentVisible(1, false);
	    }
	}*/

      	// this action should make matched nodes to render bold.
      	if (getModel().getFilterableModel().matches((TreeNode) value)) {
      	    label.setFont(derivedFont);
      	} else {
      	    label.setFont(originalFont);
      	}

      	// "Entity" node distinguishing from property nodes :
      	if (node.getLevel() == 1) {
      	    label.setFont(label.getFont().deriveFont(label.getFont().getStyle() + Font.ITALIC));
      	}

      	return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

}
