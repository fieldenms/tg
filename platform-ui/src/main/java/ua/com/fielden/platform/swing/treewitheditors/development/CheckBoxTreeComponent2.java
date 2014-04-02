package ua.com.fielden.platform.swing.treewitheditors.development;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateButtonModel.State;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.QuadristateCheckbox;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.UIManager;
import javax.swing.tree.TreePath;

/**
 * Represents the check box with four different states (see {@link State} for more information about check box states) that will be placed on the tree item. This implementation of
 * the {@link ITreeCheckingModelComponent2} must be associated with {@link DefaultTreeCheckingModel}
 * 
 * @author TG Team
 * 
 */
public class CheckBoxTreeComponent2 implements ITreeCheckingModelComponent2 {
    private final QuadristateCheckbox checkBox;
    private final MultipleCheckboxTreeModel2 model;
    private final int index;

    /**
     * Creates new {@link CheckBoxTreeComponent2} with {@link QuadristateCheckbox} and associated {@link DefaultTreeCheckingModel}
     * 
     * @param treeCheckingModel
     */
    public CheckBoxTreeComponent2(final MultipleCheckboxTreeModel2 model, final int index) {
        this.checkBox = new QuadristateCheckbox() {
            private static final long serialVersionUID = 5159933744944567866L;

            @Override
            protected void processMouseEvent(final MouseEvent e) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    processCheckboxAction((MultipleCheckboxTree2) e.getComponent().getParent().getParent());
                }
            }

            @Override
            protected void processKeyEvent(final KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        processCheckboxAction((MultipleCheckboxTree2) e.getComponent().getParent().getParent());
                        return;
                    }
                }
                super.processKeyEvent(e);
            }

            private void processCheckboxAction(final MultipleCheckboxTree2 tree) {
                final TreePath editingPath = tree.getEditingPath();
                CheckBoxTreeComponent2.this.model.getCheckingModel(index).toggleCheckingPath(editingPath);
                updateComponent(editingPath);
                grabFocus();
            }
        };
        this.checkBox.setBackground(UIManager.getColor("Tree.textBackground"));
        this.model = model;
        this.index = index;
    }

    @Override
    public QuadristateCheckbox getComponent() {
        return checkBox;
    }

    @Override
    public void updateComponent(final TreePath treePath) {
        getComponent().setOpaque(false);
        getComponent().setEnabled(model.getCheckingModel(index).isPathEnabled(treePath) /* TODO wtf? && checkboxTree.isEnabled() */);
        final boolean checked = model.getCheckingModel(index).isPathChecked(treePath);
        final boolean greyed = model.getCheckingModel(index).isPathGreyed(treePath);
        if (checked && !greyed) {
            getComponent().setState(State.CHECKED);
        }
        if (!checked && greyed) {
            getComponent().setState(State.GREY_UNCHECKED);
        }
        if (checked && greyed) {
            getComponent().setState(State.GREY_CHECKED);
        }
        if (!checked && !greyed) {
            getComponent().setState(State.UNCHECKED);
        }
    }
}
