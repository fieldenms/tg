package ua.com.fielden.platform.swing.dynamicreportstree;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTreeCellRenderer;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateButtonModel.State;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateCheckbox;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;

/**
 * This cell renderer behaves almost like {@link DefaultCheckboxTreeCellRenderer} and paints dashed rectangle around selected node.
 * 
 * A renderer for the CheckboxTree. This implementation decorates a DefaultTreeCellRenderer (i.e. a JLabel) with a checkbox, by adding a JCheckbox to the former onto a JPanel. Both
 * can be overridden by subclasses. Note that this renderer separates the checkbox form the label/icon, in that double-clicking the label/icon of this renderer does not toggle the
 * checkbox.
 * 
 * @author Jhou
 * @author boldrini
 * @author bigagli
 */
public class EntitiesTreeCellRenderer extends JPanel implements CheckboxTreeCellRenderer {
    private static final long serialVersionUID = 1L;

    /**
     * Loads an ImageIcon from the file iconFile, searching it in the classpath.Guarda un po'
     */
    protected static ImageIcon loadIcon(final String iconFile) {
	try {
	    return new ImageIcon(DefaultCheckboxTreeCellRenderer.class.getClassLoader().getResource(iconFile));
	} catch (final NullPointerException npe) { // did not find the resource
	    return null;
	}
    }

    protected QuadristateCheckbox checkBox = new QuadristateCheckbox();

    protected DefaultTreeCellRenderer label = new DefaultTreeCellRenderer();

    // @Override
    // public void doLayout() {
    // Dimension d_check = this.checkBox.getPreferredSize();
    // Dimension d_label = this.label.getPreferredSize();
    // int y_check = 0;
    // int y_label = 0;
    // if (d_check.height < d_label.height) {
    // y_check = (d_label.height - d_check.height) / 2;
    // } else {
    // y_label = (d_check.height - d_label.height) / 2;
    // }
    // this.checkBox.setLocation(0, y_check);
    // this.checkBox.setBounds(0, y_check, d_check.width, d_check.height);
    // this.label.setLocation(d_check.width, y_label);
    // this.label.setBounds(d_check.width, y_label, d_label.width,
    // d_label.height);
    // }

    public EntitiesTreeCellRenderer() {
	this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	add(this.checkBox);
	add(this.label);
	this.checkBox.setBackground(UIManager.getColor("Tree.textBackground"));
	this.setBackground(UIManager.getColor("Tree.textBackground"));
    }

    @Override
    public Dimension getPreferredSize() {
	final Dimension d_check = this.checkBox.getPreferredSize();
	final Dimension d_label = this.label.getPreferredSize();
	return new Dimension(d_check.width + d_label.width, (d_check.height < d_label.height ? d_label.height : d_check.height));
    }

    /**
     * Decorates this renderer based on the passed in components.
     */
    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object object, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	this.label.setOpaque(false);
	this.checkBox.setOpaque(false);
	this.setOpaque(false);

	/*
	 * most of the rendering is delegated to the wrapped
	 * DefaultTreeCellRenderer, the rest depends on the TreeCheckingModel
	 */
	this.label.getTreeCellRendererComponent(tree, object, selected, expanded, leaf, row, hasFocus);

	if (tree instanceof CheckboxTree) {
	    final TreeCheckingModel checkingModel = ((CheckboxTree) tree).getCheckingModel();
	    final TreePath path = tree.getPathForRow(row);
	    this.checkBox.setEnabled(checkingModel.isPathEnabled(path) && tree.isEnabled());
	    final boolean checked = checkingModel.isPathChecked(path);
	    final boolean greyed = checkingModel.isPathGreyed(path);
	    if (checked && !greyed) {
		this.checkBox.setState(State.CHECKED);
	    }
	    if (!checked && greyed) {
		this.checkBox.setState(State.GREY_UNCHECKED);
	    }
	    if (checked && greyed) {
		this.checkBox.setState(State.GREY_CHECKED);
	    }
	    if (!checked && !greyed) {
		this.checkBox.setState(State.UNCHECKED);
	    }
	}

	this.selected = selected;
	//	// this action should correct the "emptiness" of the label renderer under "Nimbus" L&F.
	//	this.setBackground(selected ? UIManager.getColor("Tree.selectionBackground") : UIManager.getColor("Tree.textBackground"));

	// setting toolTips for the each node based on "desc" of the corresponding TitledObject.
	if (object != null) {
	    final Object userObject = ((DefaultMutableTreeNode) object).getUserObject();
	    ((JComponent) this).setToolTipText((userObject instanceof TitledObject) ? ((TitledObject) userObject).getDesc() : userObject.toString());
	}

	return this;
    }

    /** Is the value currently selected. */
    protected boolean selected;

    /**
     * Paints the value. The background is filled based on selected.
     */
    @Override
    public void paint(final Graphics g) {
	Color bColor;
	if (selected) {
	    bColor = label.getBackgroundSelectionColor();
	} else {
	    bColor = label.getBackgroundNonSelectionColor();
	    if (bColor == null) {
		bColor = getBackground();
	    }
	}

	super.paint(g);

	int imageOffset = -1;
	if (selected) {
	    imageOffset = 0;
	    paintFocus(g, imageOffset, 0, getWidth() - imageOffset, getHeight(), bColor);
	}
    }

    // If drawDashedFocusIndicator is true, the following are used.
    /**
     * Background color of the tree.
     */
    private Color treeBGColor;
    /**
     * Color to draw the focus indicator in, determined from the background. color.
     */
    private Color focusBGColor;

    private void paintFocus(final Graphics g, final int x, final int y, final int w, final int h, final Color notColor) {
	final Color bsColor = label.getBorderSelectionColor();

	if (bsColor != null && (selected || !true)) {
	    g.setColor(bsColor);
	    g.drawRect(x, y, w - 1, h - 1);
	}
	if (true && notColor != null) {
	    if (treeBGColor != notColor) {
		treeBGColor = notColor;
		focusBGColor = new Color(~notColor.getRGB());
	    }
	    g.setColor(focusBGColor);
	    BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
	}
    }

    /**
     * Checks if the (x,y) coordinates are on the Checkbox.
     * 
     * @return boolean
     * @param x
     * @param y
     */
    @Override
    public boolean isOnHotspot(final int x, final int y) {
	// TODO: alternativa (ma funge???)
	// return this.checkBox.contains(x, y);
	return (this.checkBox.getBounds().contains(x, y));
    }

    @Override
    public void setBackground(Color color) {
	if (color instanceof ColorUIResource) {
	    color = null;
	}
	super.setBackground(color);
    }

    /**
     * Sets the icon used to represent non-leaf nodes that are not expanded.
     */
    public void setClosedIcon(final Icon newIcon) {
	this.label.setClosedIcon(newIcon);
    }

    /**
     * Sets the icon used to represent leaf nodes.
     */
    public void setLeafIcon(final Icon newIcon) {
	this.label.setLeafIcon(newIcon);
    }

    /**
     * Sets the icon used to represent non-leaf nodes that are expanded.
     */
    public void setOpenIcon(final Icon newIcon) {
	this.label.setOpenIcon(newIcon);
    }

}