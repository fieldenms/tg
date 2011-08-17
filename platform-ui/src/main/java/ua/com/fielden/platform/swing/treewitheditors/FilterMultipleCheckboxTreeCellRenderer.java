package ua.com.fielden.platform.swing.treewitheditors;

import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import ua.com.fielden.platform.swing.menu.filter.FilterableTreeModel;
import ua.com.fielden.platform.treemodel.rules.ITooltipProvider;

/**
 * Cell renderer for MultipleCheckboxTree with bold filtered nodes.
 * 
 * @author TG Team
 * 
 */
public class FilterMultipleCheckboxTreeCellRenderer extends MultipleCheckboxTreeCellRenderer {

    private static final long serialVersionUID = 2940223267761789273L;

    private final FilterableTreeModel model;
    private final Font originalFont;
    private final Font derivedFont;

    public FilterMultipleCheckboxTreeCellRenderer(final MultipleCheckboxTree tree, final FilterableTreeModel treeModel, final ITooltipProvider provider, final List<ITooltipProvider> toolTipProviders) {
	super(tree, provider, toolTipProviders);
	this.model = treeModel;
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
	if (((DefaultMutableTreeNode) value).getLevel() == 1) {
	    label.setFont(label.getFont().deriveFont(label.getFont().getStyle() + Font.ITALIC));
	}

	return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

}
