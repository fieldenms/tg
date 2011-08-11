package ua.com.fielden.platform.swing.dynamicreportstree;

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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.treewitheditors.CheckBoxTreeComponent;
import ua.com.fielden.platform.swing.treewitheditors.EntitiesTree;
import ua.com.fielden.platform.swing.treewitheditors.FilterMultipleCheckboxTreeCellRenderer;
import ua.com.fielden.platform.swing.treewitheditors.ITooltipProvider;
import ua.com.fielden.platform.swing.treewitheditors.ITreeCheckingModelComponent;
import ua.com.fielden.platform.swing.treewitheditors.MultipleCheckboxTree;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;

public class AbstractTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

    private static final long serialVersionUID = 7749709799167878716L;

    private final FilterMultipleCheckboxTreeCellRenderer renderer, editor;
    private final MultipleCheckboxTree checkingTree;

    private int clickCount = 1;
    private TitledObject editedObject;

    public AbstractTreeCellEditor(final EntitiesTree tree, final FilterMultipleCheckboxTreeCellRenderer renderer) {
	this.renderer = renderer;
	this.checkingTree = tree;
	final List<ITooltipProvider> toolTipProviders = new ArrayList<ITooltipProvider>();
	toolTipProviders.add(renderer.getToolTipProvider(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()));
	toolTipProviders.add(renderer.getToolTipProvider(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()));
	editor = new FilterMultipleCheckboxTreeCellRenderer(tree, tree.getModel(), renderer.getToolTipProvider(), toolTipProviders) {

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
			    editor.performMouseAction(tree.getEditingPath(), EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex());
			    updateComponent(checkBox);
			}

			private void processCriteriaAction() {
			    editor.performMouseAction(tree.getEditingPath(), EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex());
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
	editor.setPaintFocus(false);
    }

    public void setClickCount(final int clickCount) {
	this.clickCount = clickCount;
    }

    public int getClickCount() {
	return clickCount;
    }

    @Override
    public Object getCellEditorValue() {
	final TreePath selectedPath = checkingTree.getSelectionPath();
	final Object treeNode = selectedPath.getLastPathComponent();

	if (treeNode != null && treeNode instanceof DefaultMutableTreeNode) {
	    final DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) treeNode;
	    if (mutableTreeNode.getUserObject() instanceof TitledObject) {
		editedObject = (TitledObject) mutableTreeNode.getUserObject();
	    }
	}
	if (editedObject != null) {
	    return editedObject;
	}
	return null;
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
	editor.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
	return editor;
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

		final int index = renderer.getHotspotIndex(x - rect.x, y - rect.y);
		if (index > -1) {
		    if (checkingTree.getCheckingModel(index).isPathEnabled(checkingTree.getPathForRow(row))) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

}
