package ua.com.fielden.platform.swing.menu;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;

/**
 * This renderer should be used for trees that support filtering. Nodes, which are matched are highlighted with a bold font.
 * 
 * @author 01es
 * 
 */
public class FilterCellRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = -5622640897483968875L;

    private final IFilterableModel filterableModel;
    private Font originalFont;
    private Font derivedFont;

    public FilterCellRenderer(final IFilterableModel model) {
	this.filterableModel = model;

	setLeafIcon(null);
	setClosedIcon(null);
	setOpenIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	if (filterableModel.matches((TreeNode) value)) {
	    setFont(getDerivedFont());
	} else {
	    setFont(getOriginalFont());
	}
	return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    private Font getOriginalFont() {
	if (originalFont == null) {
	    originalFont = getFont();
	}
	return originalFont;
    }

    private Font getDerivedFont() {
	if (derivedFont == null) {
	    if (originalFont == null) {
		originalFont = getFont();
	    }
	    derivedFont = originalFont.deriveFont(Font.BOLD);
	}
	return derivedFont;
    }

    public IFilterableModel getFilterableModel() {
	return filterableModel;
    }

}
