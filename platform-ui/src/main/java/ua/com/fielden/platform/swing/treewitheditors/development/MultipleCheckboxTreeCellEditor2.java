package ua.com.fielden.platform.swing.treewitheditors.development;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Class that provides basic functionality for editing {@link TreeNode} meta parameters.
 *
 * @author TG Team
 *
 */
public class MultipleCheckboxTreeCellEditor2 extends AbstractCellEditor implements TreeCellEditor {

    private static final long serialVersionUID = -6872693277320582798L;

    protected final MultipleCheckboxTree2 tree;
    private final MultipleCheckboxTreeCellRenderer2 renderer;

    /**
     * Creates {@link MultipleCheckboxTreeCellEditor2} and initiates it with {@link MultipleCheckboxTree2}, {@link MultipleCheckboxTreeCellRenderer2}.
     *
     * @param tree
     * @param renderer
     * @param editorToolTipProvider
     */
    public MultipleCheckboxTreeCellEditor2(final MultipleCheckboxTree2 tree, final MultipleCheckboxTreeCellRenderer2 renderer) {
	this.tree = tree;
	this.renderer = renderer;
    }

    @Override
    public Object getCellEditorValue() {
	final TreePath editedPath = tree.getEditingPath();
	return editedPath.getLastPathComponent().toString();
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
	return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
    }

    @Override
    public boolean isCellEditable(final EventObject event) {

	if (event == null) {
	    for (int modelCounter = 0; modelCounter < tree.getSpecificModel().getCheckingModelCount(); modelCounter++) {
		if (tree.getSpecificModel().getCheckingModel(modelCounter).isPathEnabled(tree.getSelectionPath())) {
		    return true;
		}
	    }
	}

	if(event instanceof MouseEvent){
	    final MouseEvent e = (MouseEvent) event;
	    if (!e.isControlDown() && !e.isConsumed()) {
		final int x = e.getX();
		final int y = e.getY();
		final int row = tree.getRowForLocation(x, y);
		final Rectangle rect = tree.getRowBounds(row);
		if (rect != null) {

		    final TreePath path = tree.getPathForRow(row);

		    final boolean isCriteriaEnable = tree.getSpecificModel().getCheckingModel(0).isPathEnabled(path);
		    final boolean isResultantEnable = tree.getSpecificModel().getCheckingModel(1).isPathEnabled(path);

		    final TreeNode value = (TreeNode)path.getLastPathComponent();
		    final boolean selected = tree.isPathSelected(path);
		    final boolean expanded = tree.isExpanded(path);
		    final boolean leaf = value.isLeaf();

		    final Component editingComponent =  renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, true);
		    final Dimension editorSize = editingComponent.getPreferredSize();
		    editingComponent.setBounds(0, 0, editorSize.width, editorSize.height);
		    editingComponent.doLayout();

		    final int index = getCheckboxIndex(x - rect.x, y - rect.y);

		    if (index == 0 && isCriteriaEnable) {
			return true;
		    }else if(index == 1 && isResultantEnable){
			return true;
		    }else{
			return canEditPath(path);
		    }

		}
	    }
	}
	return false;
    }

    /**
     * Returns tree associated with this {@link MultipleCheckboxTreeCellEditor2}.
     *
     * @return
     */

    public MultipleCheckboxTree2 getTree() {
	return tree;
    }

    /**
     * Override this to provide specific logic that determines whether specified path can be edited or not.
     *
     * @param path
     * @return
     */
    protected boolean canEditPath(final TreePath path){
	return false;
    }

    /**
     * Returns the index of {@link ITreeCheckingModelComponent2} that contains the point with specified x and y coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    private int getCheckboxIndex(final int x, final int y){
	for (int componentCounter = 0; componentCounter < tree.getSpecificModel().getCheckingModelCount(); componentCounter++) {
	    if (renderer.checkingComponents.get(componentCounter).getComponent().getBounds().contains(x, y)) {
		return componentCounter;
	    }
	}
	return -1;
    }

    public MultipleCheckboxTreeCellRenderer2 getRenderer() {
	return renderer;
    }

}
