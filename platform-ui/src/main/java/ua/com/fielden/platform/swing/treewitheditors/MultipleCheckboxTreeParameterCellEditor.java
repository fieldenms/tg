package ua.com.fielden.platform.swing.treewitheditors;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateButtonModel.State;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateCheckbox;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.ITooltipProvider;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;

/**
 * Class that provides basic functionality for editing {@link TreeNode} meta parameters.
 * 
 * @author oleh
 * 
 */
public abstract class MultipleCheckboxTreeParameterCellEditor extends AbstractCellEditor implements TreeCellEditor {

    private static final long serialVersionUID = -6872693277320582798L;

    private static final int hGap = 10;

    private final MultipleCheckboxTree checkingTree;
    private final MultipleCheckboxTreeCellRendererWithParameter renderer;

    private final FilterMultipleCheckboxTreeCellRenderer defaultRenderer;
    private final ITooltipProvider editorTootipProvider;
    private final EntitiesTreeModel actualModel;

    private int clickCount = 1;
    private TitledObject editedObject;

    /**
     * Creates {@link MultipleCheckboxTreeParameterCellEditor} and initiates it with {@link EntitiesTree}, {@link MultipleCheckboxTreeCellRendererWithParameter} and
     * {@link ITooltipProvider} for editor.
     * 
     * @param tree
     * @param renderer
     * @param editorToolTipProvider
     */
    public MultipleCheckboxTreeParameterCellEditor(final EntitiesTree tree, final MultipleCheckboxTreeCellRendererWithParameter renderer, final ITooltipProvider editorToolTipProvider) {
	this.renderer = renderer;
	this.editorTootipProvider = editorToolTipProvider;
	this.checkingTree = tree;
	if (tree.getModel().getOriginModel() instanceof EntitiesTreeModel) {
	    this.actualModel = (EntitiesTreeModel) tree.getModel().getOriginModel();
	} else {
	    this.actualModel = null;
	}
	final List<ITooltipProvider> toolTipProviders = new ArrayList<ITooltipProvider>();
	toolTipProviders.add(renderer.getToolTipProvider(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()));
	toolTipProviders.add(renderer.getToolTipProvider(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()));

	this.defaultRenderer = new FilterMultipleCheckboxTreeCellRenderer(tree, tree.getModel(), renderer.getToolTipProvider(), toolTipProviders) {

	    private static final long serialVersionUID = 7635815734481860305L;

	    @Override
	    protected ITreeCheckingModelComponent getCheckingComponent(final TreeCheckingModel treeCheckingModel) {
		final int index = tree.getCheckingModelIndex(treeCheckingModel);
		if (index < 0) {
		    return null;
		}
		return new CheckBoxTreeComponent(tree, index) {

		    private final QuadristateCheckbox checkBox = new QuadristateCheckbox() {

			private static final long serialVersionUID = 6209289011684087570L;

			@Override
			protected void processMouseEvent(final MouseEvent e) {
			    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				if (index == EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()) {
				    processCriteriaAction();
				} else if (index == EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()) {
				    processResultantAction();
				}
			    }
			}

			@Override
			protected void processKeyEvent(final KeyEvent e) {
			    if (e.getID() == KeyEvent.KEY_PRESSED) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
				    if (index == EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()) {
					processCriteriaAction();
				    } else if (index == EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()) {
					processResultantAction();
				    }
				    return;
				}
			    }
			    super.processKeyEvent(e);
			}

			private void processResultantAction() {
			    defaultRenderer.performMouseAction(tree.getEditingPath(), EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex());
			    updateComponent(checkBox);
			}

			private void processCriteriaAction() {
			    defaultRenderer.performMouseAction(tree.getEditingPath(), EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex());
			    updateComponent(checkBox);
			}

			private void updateComponent(final QuadristateCheckbox quadriCheckBox) {
			    quadriCheckBox.setOpaque(false);
			    final TreePath path = tree.getEditingPath();
			    quadriCheckBox.setEnabled(tree.getCheckingModel(index).isPathEnabled(path) && tree.isEnabled());
			    final boolean checked = tree.getCheckingModel(index).isPathChecked(path);
			    final boolean greyed = tree.getCheckingModel(index).isPathGreyed(path);
			    if (checked && !greyed) {
				quadriCheckBox.setState(State.CHECKED);
			    }
			    if (!checked && greyed) {
				quadriCheckBox.setState(State.GREY_UNCHECKED);
			    }
			    if (checked && greyed) {
				quadriCheckBox.setState(State.GREY_CHECKED);
			    }
			    if (!checked && !greyed) {
				quadriCheckBox.setState(State.UNCHECKED);
			    }
			    quadriCheckBox.grabFocus();
			}

		    };

		    @Override
		    public QuadristateCheckbox getComponent() {
			return checkBox;
		    }
		};
	    }
	};
	tree.getModel().addFilterListener(new IFilterListener() {

	    @Override
	    public boolean nodeVisibilityChanged(final TreeNode treeNode, final boolean prevValue, final boolean newValue) {
		return false;
	    }

	    @Override
	    public void postFilter(final IFilterableModel model) {

	    }

	    @Override
	    public void preFilter(final IFilterableModel model) {
		fireEditingStopped();
	    }

	});
	defaultRenderer.setPaintFocus(false);
    }

    /**
     * Set the click count when to start path editing.
     * 
     * @param clickCount
     */
    public void setClickCount(final int clickCount) {
	this.clickCount = clickCount;
    }

    /**
     * Returns click count when path starts editing.
     * 
     * @return
     */
    public int getClickCount() {
	return clickCount;
    }

    @Override
    public Object getCellEditorValue() {
	final TreePath selectedPath = checkingTree.getSelectionPath();
	editedObject = getTitledObjectForPath(selectedPath);
	setParameterValueFor(selectedPath);
	return editedObject;
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
	defaultRenderer.setOpaque(false);
	defaultRenderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
	final JComponent editingComponent = getParameterEditingComponent((DefaultMutableTreeNode) value);
	//final double width = editingComponent != null ? editingComponent.getPreferredSize().getWidth() : 0;
	final JPanel editor = new JPanel(new MigLayout("fill, insets 0", "[]" + hGap + "[]", "[" + getMinHeight() + "::,c,grow]"));
	editor.setOpaque(false);
	editor.add(defaultRenderer);
	if (editingComponent != null) {
	    editor.add(editingComponent);
	    editingComponent.setOpaque(false);
	    editingComponent.setToolTipText(editorTootipProvider.getToolTip((TreeNode) value));
	}
	return editor;
    }

    /**
     * Returns {@link TitledObject} for path if it's exists. Otherwise returns null;
     * 
     * @param path
     * @return
     */
    final protected TitledObject getTitledObjectForPath(final TreePath path) {
	final Object treeNode = path.getLastPathComponent();
	TitledObject titledObject = null;
	if (treeNode != null && treeNode instanceof DefaultMutableTreeNode) {
	    final DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) treeNode;
	    if (mutableTreeNode.getUserObject() instanceof TitledObject) {
		titledObject = (TitledObject) mutableTreeNode.getUserObject();
	    }
	}
	return titledObject;
    }

    @Override
    public boolean isCellEditable(final EventObject event) {
	if (!(event instanceof MouseEvent)) {
	    if (event == null) {
		for (int modelCounter = 0; modelCounter < checkingTree.getCheckingModelCount(); modelCounter++) {
		    if (checkingTree.getCheckingModel(modelCounter).isPathEnabled(checkingTree.getSelectionPath())) {
			return true;
		    }
		}
		return false;
	    } else {
		return false;
	    }
	}
	final MouseEvent e = (MouseEvent) event;
	if (!e.isControlDown() && e.getClickCount() >= clickCount && !e.isConsumed()) {
	    final int x = e.getX();
	    final int y = e.getY();
	    final int row = checkingTree.getRowForLocation(x, y);
	    final Rectangle rect = checkingTree.getRowBounds(row);
	    if (rect != null) {
		final Object treeNode = checkingTree.getPathForRow(row).getLastPathComponent();
		TitledObject titledObject = null;
		if (treeNode != null && treeNode instanceof DefaultMutableTreeNode) {
		    final DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) treeNode;
		    if (mutableTreeNode.getUserObject() instanceof TitledObject) {
			titledObject = (TitledObject) mutableTreeNode.getUserObject();
		    }
		}
		final boolean isTitledObject = titledObject != null;
		final boolean isResultantEnable = checkingTree.getCheckingModel(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).isPathEnabled(checkingTree.getPathForRow(row));
		final boolean isCriteriaEnable = checkingTree.getCheckingModel(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).isPathEnabled(checkingTree.getPathForRow(row));

		final int index = renderer.getHotspotIndex(x - rect.x, y - rect.y);
		if (index < 0) {
		    return canEditPath(checkingTree.getPathForRow(row));
		} else if (index == EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()) {
		    if (isTitledObject && isCriteriaEnable) {
			return true;
		    }
		} else if (index == EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()) {
		    if (isTitledObject && isResultantEnable) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    /**
     * Returns tree for which this {@link MultipleCheckboxTreeParameterCellEditor} was created.
     * 
     * @return
     */
    protected MultipleCheckboxTree getCheckingTree() {
	return checkingTree;
    }

    /**
     * Returns the {@link EntitiesTreeModel} for which this {@link MultipleCheckboxTreeParameterCellEditor} was created.
     * 
     * @return
     */
    protected EntitiesTreeModel getActualModel() {
	return actualModel;
    }

    /**
     * Returns the component for editing {@code value} tree node.
     */
    abstract protected JComponent getParameterEditingComponent(final DefaultMutableTreeNode value);

    /**
     * Set the selected value of the editing component for the edited path.
     * 
     * @param path
     */
    abstract protected void setParameterValueFor(final TreePath path);

    /**
     * Returns true if the path can be edited. This routine will be invoked when user clicked the tree node caption.
     * 
     * @param path
     * @return
     */
    abstract protected boolean canEditPath(final TreePath path);

    /**
     * Returns minimum height of the editor.
     * 
     * @return
     */
    abstract protected double getMinHeight();
}
