package ua.com.fielden.platform.swing.menu;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;

public class FilterCellRendererWithIcon extends FilterCellRenderer {

    public FilterCellRendererWithIcon(final IFilterableModel model, final DefaultTreeCellRenderer renderer) {
	super(model, renderer);
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	final JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	if (value instanceof TreeMenuItem && ((TreeMenuItem<?>)value).getIcon() != null) {
	    label.setIcon(((TreeMenuItem<?>)value).getIcon());
	}
	return label;
    }

}
