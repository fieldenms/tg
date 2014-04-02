package ua.com.fielden.platform.swing.treewitheditors.development;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultTreeCheckingModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import ua.com.fielden.platform.swing.menu.filter.AbstractFilterableTreeModel;

/**
 * A multiple checkbox tree with a model.
 * 
 * @author TG Team
 * 
 */
public class MultipleCheckboxTree2 extends Tree {
    private static final long serialVersionUID = -239641941602548337L;
    private MultipleCheckboxTreeModel2 model;

    /**
     * Creates {@link MultipleCheckboxTree2} with specified model.
     * 
     */
    public MultipleCheckboxTree2(final MultipleCheckboxTreeModel2 model) {
        super(model);

        setSpecificModel(model);

        ToolTipManager.sharedInstance().registerComponent(this);

        setEditable(true);
        setCellRenderer(new MultipleCheckboxTreeCellRenderer2(model));
        final MultipleCheckboxTreeCellRenderer2 renderer = new MultipleCheckboxTreeCellRenderer2(model);
        setCellEditor(new MultipleCheckboxTreeCellEditor2(this, renderer));

        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setShowsRootHandles(true);
        putClientProperty("JTree.lineStyle", "Angled");// for Metal L&F

        final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        final String EDITING_MODE = "Start or stop editing selected path";
        getActionMap().put(EDITING_MODE, createEditingModeAction());
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, EDITING_MODE);
    }

    /**
     * Sets the TreeModel and links it to the existing {@link TreeCheckingModel} s.
     */
    @Override
    public void setModel(final TreeModel newModel) {
        super.setModel(newModel);

        if (newModel instanceof AbstractFilterableTreeModel) {
            final AbstractFilterableTreeModel filterableTreeModel = (AbstractFilterableTreeModel) newModel;
            setSpecificModel((MultipleCheckboxTreeModel2) filterableTreeModel.getOriginModel());
        } else {
            setSpecificModel((MultipleCheckboxTreeModel2) newModel);
        }

        for (int modelCounter = 0; modelCounter < this.model.getCheckingModelCount(); modelCounter++) {
            final TreeCheckingModel checkingModel = this.model.getCheckingModel(modelCounter);
            if (checkingModel != null && checkingModel instanceof DefaultTreeCheckingModel) {
                ((DefaultTreeCheckingModel) checkingModel).setTreeModel(newModel);
            }
        }
    }

    public MultipleCheckboxTreeModel2 getSpecificModel() {
        return model;
    }

    private void setSpecificModel(final MultipleCheckboxTreeModel2 model) {
        this.model = model;
        this.model.makeTreeRepaintable(this);
    }

    private Action createEditingModeAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = -5565139442029535686L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (isEditing()) {
                    stopEditing();
                } else {
                    startEditingAtPath(getSelectionPath());
                }
            }
        };
    }
}
