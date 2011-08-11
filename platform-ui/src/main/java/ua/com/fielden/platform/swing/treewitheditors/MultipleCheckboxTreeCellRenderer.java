package ua.com.fielden.platform.swing.treewitheditors;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingMode;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;

/**
 * Implementation of the {@link IMultipleCheckboxTreeCellRenderer}
 * 
 * @author oleh
 * @author boldrini
 * @author bigagli
 * 
 */
public class MultipleCheckboxTreeCellRenderer extends JPanel implements IMultipleCheckboxTreeCellRenderer {

    private static final long serialVersionUID = 4508173236279114268L;

    protected final DefaultTreeCellRenderer label = new DefaultTreeCellRenderer();

    private final List<ITreeCheckingModelComponent> checkingComponents;

    private final List<ITooltipProvider> toolTipProviders;

    private final ITooltipProvider toolTipProvider;

    private final MultipleCheckboxTree tree;

    private boolean paintFocus = true;

    public MultipleCheckboxTreeCellRenderer(final MultipleCheckboxTree tree, final ITooltipProvider provider, final List<ITooltipProvider> toolTipProviders) {
	super(new FlowLayout(FlowLayout.LEFT, 0, 0));
	this.tree = tree;
	this.setBackground(UIManager.getColor("Tree.textBackground"));
	this.checkingComponents = new ArrayList<ITreeCheckingModelComponent>(tree.getCheckingModelCount());
	this.toolTipProviders = new ArrayList<ITooltipProvider>(tree.getCheckingModelCount());
	this.toolTipProvider = provider;
	for (int modelIndex = 0; modelIndex < tree.getCheckingModelCount(); modelIndex++) {
	    this.checkingComponents.add(getCheckingComponent(tree.getCheckingModel(modelIndex)));
	    add(checkingComponents.get(modelIndex).getComponent());
	    if (toolTipProviders != null && modelIndex < toolTipProviders.size()) {
		this.toolTipProviders.add(toolTipProviders.get(modelIndex));
	    } else {
		this.toolTipProviders.add(null);
	    }
	}
	add(label);
    }

    @Override
    public Dimension getPreferredSize() {
	final Dimension d_label = this.label.getPreferredSize();
	int width = d_label.width, height = d_label.height;
	for (int componentCounter = 0; componentCounter < tree.getCheckingModelCount(); componentCounter++) {
	    final Dimension d_component = checkingComponents.get(componentCounter).getComponent().getPreferredSize();
	    width += d_component.width;
	    height = height < d_component.height ? d_component.height : height;
	}
	return new Dimension(width, height);
    }

    @Override
    public int getHotspotIndex(final int x, final int y) {
	for (int componentCounter = 0; componentCounter < tree.getCheckingModelCount(); componentCounter++) {
	    if (checkingComponents.get(componentCounter).isOnHotspot(x, y)) {
		return componentCounter;
	    }
	}
	return -1;
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	this.label.setOpaque(false);
	this.setOpaque(false);

	/*
	 * most of the rendering is delegated to the wrapped DefaultTreeCellRenderer, the rest depends on the TreeCheckingModel
	 */
	this.label.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

	setCheckingComponentVisible(true);

	if (tree instanceof MultipleCheckboxTree) {
	    final MultipleCheckboxTree multipleCheckboxTree = (MultipleCheckboxTree) tree;
	    for (int componentCounter = 0; componentCounter < multipleCheckboxTree.getCheckingModelCount(); componentCounter++) {
		checkingComponents.get(componentCounter).updateComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	    }
	    final TitledObject title = TitledObject.extractTitleFromTreeNode(value);
	    if (title != null && title.getType() == null) {
		setCheckingComponentVisible(false);
	    }
	    if (value instanceof DefaultMutableTreeNode) {
		final TitledObject parentTitle = TitledObject.extractTitleFromTreeNode(((DefaultMutableTreeNode) value).getParent());
		if (parentTitle != null && parentTitle.getType() != null && AbstractUnionEntity.class.isAssignableFrom(parentTitle.getType())) {
		    setCheckingComponentVisible(1, false);
		}
	    }
	}

	this.selected = selected;
	final ITooltipProvider provider = getToolTipProvider();
	if (provider != null) {
	    this.label.setToolTipText(provider.getToolTip((TreeNode) value));
	}
	for (int componentCounter = 0; componentCounter < this.tree.getCheckingModelCount(); componentCounter++) {
	    final Component checkingComponent = checkingComponents.get(componentCounter).getComponent();
	    final ITooltipProvider componentToolTipProvider = getToolTipProvider(componentCounter);
	    if (checkingComponent instanceof JComponent && componentToolTipProvider != null) {
		((JComponent) checkingComponent).setToolTipText(componentToolTipProvider.getToolTip((TreeNode) value));
	    }
	}
	return this;
    }

    /**
     * Set specified {@link ITreeCheckingModelComponent} visible property to specified one.
     * 
     * @param index
     * @param visible
     */
    private void setCheckingComponentVisible(final int index, final boolean visible) {
	final ITreeCheckingModelComponent checkingComponent = checkingComponents.get(index);
	if (checkingComponent.getComponent().isVisible() != visible) {
	    checkingComponent.getComponent().setVisible(visible);
	}
    }

    /**
     * Set the checking components visible property to specified one.
     * 
     * @param visible
     */
    private void setCheckingComponentVisible(final boolean visible) {
	for (int componentIndex = 0; componentIndex < tree.getCheckingModelCount(); componentIndex++) {
	    setCheckingComponentVisible(componentIndex, visible);
	}
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
	if (selected && isPaintFocus()) {
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

    /**
     * Returns {@link ITreeCheckingModelComponent} for the specified {@link TreeCheckingMode} instance.
     * 
     * @param treeCheckingModel
     *            - specified {@link TreeCheckingModel} instance.
     */
    protected ITreeCheckingModelComponent getCheckingComponent(final TreeCheckingModel treeCheckingModel) {
	final int index = tree.getCheckingModelIndex(treeCheckingModel);
	if (index < 0) {
	    return null;
	}
	return new CheckBoxTreeComponent(tree, index);
    }

    @Override
    public void performMouseAction(final TreePath treePath, final int checkingComponentIndex) {
	if (checkingComponentIndex < 0 || checkingComponentIndex >= tree.getCheckingModelCount() || treePath == null) {
	    return;
	}
	checkingComponents.get(checkingComponentIndex).actionPerformed(treePath);
    }

    public void setPaintFocus(final boolean paintFocus) {
	this.paintFocus = paintFocus;
    }

    public boolean isPaintFocus() {
	return paintFocus;
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
	final Component selectedComponent = getComponetLocatedAt(event.getX(), event.getY());
	if (selectedComponent != null && selectedComponent instanceof JComponent) {
	    return ((JComponent) selectedComponent).getToolTipText();
	}
	return "";
    }

    private Component getComponetLocatedAt(final int x, final int y) {
	for (int componentCounter = 0; componentCounter < this.getComponentCount(); componentCounter++) {
	    if (this.getComponent(componentCounter).getBounds().contains(x, y) && this.getComponent(componentCounter).isVisible()) {
		return getComponent(componentCounter);
	    }
	}
	return null;
    }

    @Override
    public ITooltipProvider getToolTipProvider(final int checkBoxIndex) {
	if ((checkBoxIndex < 0) || (checkBoxIndex >= tree.getCheckingModelCount())) {
	    return null;
	}
	return toolTipProviders.get(checkBoxIndex);
    }

    @Override
    public ITooltipProvider getToolTipProvider() {
	return toolTipProvider;
    }

}
