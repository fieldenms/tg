package ua.com.fielden.platform.swing.treewitheditors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.domaintree.ITooltipProvider;

public abstract class MultipleCheckboxTreeCellRendererWithParameter extends JPanel implements IMultipleCheckboxTreeCellRenderer {

    private static final long serialVersionUID = 8009223588555582433L;

    private static final int hGap = 10;

    private final MultipleCheckboxTreeCellRenderer renderer;
    private final JLabel label = new JLabel("");
    private final ITooltipProvider labelTooltipProvider;
    private double rendererHeight;

    public MultipleCheckboxTreeCellRendererWithParameter(final MultipleCheckboxTreeCellRenderer renderer, final ITooltipProvider labelTooltipProvider) {
	super(new FlowLayout(FlowLayout.LEFT, 0, 0));
	this.renderer = renderer;
	this.labelTooltipProvider = labelTooltipProvider;
	this.renderer.setPaintFocus(false);
	label.setEnabled(false);
	add(renderer);
	add(label);
    }

    /**
     * Set the minimum renderer height.
     * 
     * @param rendererHeight
     */
    protected void setRendererHeight(final double rendererHeight) {
	this.rendererHeight = rendererHeight;
    }

    @Override
    public int getHotspotIndex(final int x, final int y) {
	return renderer.getHotspotIndex(x, y);
    }

    @Override
    public void performMouseAction(final TreePath treePath, final int checkingComponentIndex) {
	renderer.performMouseAction(treePath, checkingComponentIndex);
    }

    @Override
    public ITooltipProvider getToolTipProvider() {
	return renderer.getToolTipProvider();
    }

    @Override
    public ITooltipProvider getToolTipProvider(final int checkBoxIndex) {
	return renderer.getToolTipProvider(checkBoxIndex);
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
	doLayout();
	final Component selectedComponent = getComponentLocatedAt(event.getX(), event.getY());
	if (selectedComponent != null && selectedComponent instanceof JComponent) {
	    return ((JComponent) selectedComponent).getToolTipText();
	}
	return null;
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	label.setOpaque(false);
	setOpaque(false);
	renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	final MultipleCheckboxTree multipleCheckboxTree = (MultipleCheckboxTree) tree;

	configureParameterLabel(multipleCheckboxTree, (DefaultMutableTreeNode) value);

	// Setting tool tip for label.
	if (labelTooltipProvider != null) {
	    this.label.setToolTipText(labelTooltipProvider.getToolTip((TreeNode) value));
	}
	return this;
    }

    abstract protected void configureParameterLabel(final MultipleCheckboxTree multipleCheckboxTree, final DefaultMutableTreeNode value);

    private Component getComponentLocatedAt(final int x, final int y) {
	for (int componentCounter = 0; componentCounter < this.renderer.getComponentCount(); componentCounter++) {
	    if (this.renderer.getComponent(componentCounter).getBounds().contains(x, y) && this.renderer.getComponent(componentCounter).isVisible()) {
		return renderer.getComponent(componentCounter);
	    }
	}
	if (label.getBounds().contains(x, y)) {
	    return label;
	}
	return null;
    }

    @Override
    public void doLayout() {
	renderer.doLayout();
	renderer.setSize(renderer.getPreferredSize());
	final Dimension rSize = renderer.getSize();
	renderer.setLocation(0, (int) Math.ceil(Math.abs(rendererHeight - rSize.getHeight()) / 2.0));
	final FontMetrics metrics = new FontMetrics(label.getFont()) {
	    private static final long serialVersionUID = 807490729972704879L;
	};
	final Rectangle2D rect = metrics.getStringBounds(label.getText(), null);
	label.setLocation(rSize.width + hGap, (int) Math.ceil(Math.abs(rendererHeight - rSize.getHeight()) / 2.0));
	label.setSize(new Dimension((int) rect.getWidth(), rSize.height));
    }

    @Override
    public Dimension getPreferredSize() {
	final Dimension renderDim = renderer.getPreferredSize();
	int width = renderDim.width + hGap;
	double minHeight = rendererHeight;
	final FontMetrics metrics = new FontMetrics(label.getFont()) {
	    private static final long serialVersionUID = 807490729972704879L;
	};
	final Rectangle2D rect = metrics.getStringBounds(label.getText(), null);
	width += (int) rect.getWidth();
	if (minHeight < rect.getHeight()) {
	    minHeight = (int) rect.getHeight();
	}
	return new Dimension(width, (int) minHeight);
    }

    /**
     * Returns label that holds the information about the parameter configuration.
     * 
     * @return
     */
    protected JLabel getLabel() {
	return label;
    }

}
