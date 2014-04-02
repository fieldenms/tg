package ua.com.fielden.platform.swing.menu;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
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

    private static final long serialVersionUID = 3895081420054526516L;

    private final IFilterableModel filterableModel;

    private Font originalFont;
    private Font derivedFont;

    protected final DefaultTreeCellRenderer treeRenderer;

    public FilterCellRenderer(final IFilterableModel model, final DefaultTreeCellRenderer treeRenderer) {
        this.filterableModel = model;
        this.treeRenderer = treeRenderer;
        treeRenderer.setLeafIcon(null);
        treeRenderer.setClosedIcon(null);
        treeRenderer.setOpenIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        final JLabel label = (JLabel) treeRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (filterableModel.matches((TreeNode) value)) {
            label.setFont(getDerivedFont(label));
        } else {
            label.setFont(getOriginalFont(label));
        }
        return label;
    }

    private Font getOriginalFont(final JLabel label) {
        if (originalFont == null) {
            originalFont = label.getFont();
        }
        return originalFont;
    }

    private Font getDerivedFont(final JLabel label) {
        if (derivedFont == null) {
            if (originalFont == null) {
                originalFont = label.getFont();
            }
            derivedFont = originalFont.deriveFont(Font.BOLD);
        }
        return derivedFont;
    }

    public IFilterableModel getFilterableModel() {
        return filterableModel;
    }

}
