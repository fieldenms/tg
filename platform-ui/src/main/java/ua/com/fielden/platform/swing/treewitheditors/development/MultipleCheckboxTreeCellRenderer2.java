package ua.com.fielden.platform.swing.treewitheditors.development;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * {@link TreeCellRenderer} for the {@link MultipleCheckboxTree2}.
 *
 * @author TG Team
 *
 */
public class MultipleCheckboxTreeCellRenderer2 extends JPanel implements TreeCellRenderer {

    private static final long serialVersionUID = 8994506736938836724L;

    protected final DefaultTreeCellRenderer label;

    protected final List<ITreeCheckingModelComponent2> checkingComponents;

    protected final MultipleCheckboxTree2 tree;

    /**
     * Initiates this {@link MultipleCheckboxTreeCellRenderer2} with specified {@link MultipleCheckboxTree2} instance.
     *
     * @param tree
     */
    public MultipleCheckboxTreeCellRenderer2(final MultipleCheckboxTree2 tree){

	this.tree = tree;
	this.label = new DefaultTreeCellRenderer();
	this.checkingComponents = new ArrayList<ITreeCheckingModelComponent2>(tree.getSpecificModel().getCheckingModelCount());

	this.setBackground(UIManager.getColor("Tree.textBackground"));
	for (int modelIndex = 0; modelIndex < tree.getSpecificModel().getCheckingModelCount(); modelIndex++) {
	    this.checkingComponents.add(new CheckBoxTreeComponent2(tree, modelIndex));
	}

	initView();
    }

    @Override
    public Dimension getPreferredSize() {
	final Dimension d_label = this.label.getPreferredSize();
	int width = d_label.width, height = d_label.height;
	for (int componentCounter = 0; componentCounter < tree.getSpecificModel().getCheckingModelCount(); componentCounter++) {
	    final Dimension d_component = checkingComponents.get(componentCounter).getComponent().getPreferredSize();
	    width += d_component.width;
	    height = height < d_component.height ? d_component.height : height;
	}
	return new Dimension(width, height);
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
	if(this.tree == tree){
	    label.setOpaque(false);
	    setOpaque(false);

	    final TreePath path = this.tree.getPathForRow(row);
	    /*
	     * most of the rendering is delegated to the wrapped DefaultTreeCellRenderer, the rest depends on the TreeCheckingModel
	     */
	    label.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	    label.setToolTipText(getLabelToolTipText(path));


	    for (int componentCounter = 0; componentCounter < this.tree.getSpecificModel().getCheckingModelCount(); componentCounter++) {
		checkingComponents.get(componentCounter).updateComponent(path);
		checkingComponents.get(componentCounter).getComponent().setToolTipText(getCheckingComponentToolTipText(componentCounter, path));
	    }
	    return this;
	}
	return null;
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
	final Component selectedComponent = getComponetLocatedAt(event.getX(), event.getY());
	if (selectedComponent != null && selectedComponent instanceof JComponent) {
	    return ((JComponent) selectedComponent).getToolTipText();
	}
	return "";
    }

    /**
     * Returns tree associated with this {@link MultipleCheckboxTreeCellRenderer2}.
     *
     * @return
     */
    public MultipleCheckboxTree2 getTree() {
	return tree;
    }

    /**
     * Override this method to provide custom components layout.
     */
    protected void initView(){
	setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

	for (int modelIndex = 0; modelIndex < tree.getSpecificModel().getCheckingModelCount(); modelIndex++) {
	    add(checkingComponents.get(modelIndex).getComponent());
	}
	add(label);
    }

    /**
     * Override this method to provide tool tip text for label.
     *
     * @param treePath
     * @return
     */
    protected String getLabelToolTipText(final TreePath treePath){
	return treePath != null ? treePath.getLastPathComponent().toString(): null;
    }

    /**
     * Override this method to provide tool tip text for checking component.
     *
     * @param index
     * @param treePath
     * @return
     */
    protected String getCheckingComponentToolTipText(final int index, final TreePath treePath){
	return treePath!=null ? treePath.getLastPathComponent().toString() : null;
    }

    /**
     * Returns the component that contains the point specified with x and y coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    private Component getComponetLocatedAt(final int x, final int y) {
	for (int componentCounter = 0; componentCounter < this.getComponentCount(); componentCounter++) {
	    if (this.getComponent(componentCounter).getBounds().contains(x, y) && this.getComponent(componentCounter).isVisible()) {
		return getComponent(componentCounter);
	    }
	}
	return null;
    }

}
