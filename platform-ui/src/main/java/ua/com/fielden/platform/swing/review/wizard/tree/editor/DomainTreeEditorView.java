package ua.com.fielden.platform.swing.review.wizard.tree.editor;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.editor.ExpressionEditorView;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EditableEntitiesTree;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTree2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreeModel2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreePanel;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.utils.Pair;

public class DomainTreeEditorView<T extends AbstractEntity<?>> extends BasePanel {

    private static final long serialVersionUID = 268187881676011630L;

    private final DomainTreeEditorModel<T> domainTreeEditorModel;

    private final ExpressionEditorView editorView;

    public DomainTreeEditorView(final DomainTreeEditorModel<T> domainTreeEditorModel){
	super(new MigLayout("fill, insets 0", "[fill, grow]", "[fill, grow][]"));
	this.domainTreeEditorModel = domainTreeEditorModel;

	//Configuring the entities tree.
	final EntitiesTreeModel2 treeModel = domainTreeEditorModel.createTreeModel();
	final EditableEntitiesTree tree = new EditableEntitiesTree(treeModel,//
		domainTreeEditorModel.getExpressionModel().getNewAction(),//
		domainTreeEditorModel.getExpressionModel().getEditAction(),//
		domainTreeEditorModel.getCopyAction(),//
		domainTreeEditorModel.getExpressionModel().getDeleteAction());
	tree.addMouseListener(createPropertyChosenListener(tree));
	tree.getSelectionModel().addTreeSelectionListener(createCalculatedPropertySelectionListener(tree));
	final EntitiesTreePanel treePanel = new EntitiesTreePanel(tree);
	add(treePanel, "wrap");

	//Configuring the expression editor.
	editorView = new ExpressionEditorView(domainTreeEditorModel.getExpressionModel());
	domainTreeEditorModel.addPropertyEditListener(createPropertyEditListener(tree));
    }

    /**
     * Returns the associated wizard model.
     *
     * @return
     */
    public DomainTreeEditorModel<T> getModel(){
	return domainTreeEditorModel;
    }

    @Override
    public String getInfo() {
	// TODO Auto-generated method stub
	return null;
    }

    private IPropertyEditListener createPropertyEditListener(final EntitiesTree2 tree) {
	return new IPropertyEditListener() {

	    @Override
	    public void startEdit() {
		tree.setEditable(false);
		setEditorViewVisible(true);
	    }

	    @Override
	    public void finishEdit() {
		tree.setEditable(true);
		tree.startEditingAtPath(tree.getSelectionPath());
		setEditorViewVisible(false);
	    }
	};
    }

    private TreeSelectionListener createCalculatedPropertySelectionListener(final JTree tree) {
	return new TreeSelectionListener() {

	    @Override
	    public void valueChanged(final TreeSelectionEvent e) {
		final boolean isSeleted = tree.getSelectionModel().isPathSelected(e.getPath());
		getModel().getPropertySelectionModel().propertyStateChanged(getUserObjectFor(e.getPath()).getValue(), isSeleted);
	    }
	};
    }

    private MouseListener createPropertyChosenListener(final JTree tree) {
	return new MouseAdapter() {
	    @Override
	    public void mousePressed(final MouseEvent e) {
		final int x = e.getX();
		final int y = e.getY();
		final TreePath path = tree.getPathForLocation(x, y);
		if (path != null && getModel().getExpressionModel().getState() != UmState.VIEW) {
		    getModel().getExpressionModel().getPropertySelectionModel().propertyStateChanged(getUserObjectFor(path).getValue(),true);
		}
	    }
	};
    }

    /**
     * Shows or hide editor's panel.
     *
     * @param visible
     */
    private void setEditorViewVisible(final boolean visible){
	if(visible){
	    if(editorView.getParent() == null){
		add(editorView);
		validate();
		repaint();
	    }
	} else {
	    if(editorView.getParent() == this){
		remove(editorView);
		validate();
		repaint();
	    }
	}
    }

    /**
     * Returns the user object for the last component of the specified tree path.
     *
     * @param path
     * @return
     */
    @SuppressWarnings("unchecked")
    private Pair<Class<?>, String> getUserObjectFor(final TreePath path){
	final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
	return (Pair<Class<?>, String>) node.getUserObject();

    }
}
