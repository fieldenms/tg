package ua.com.fielden.platform.swing.review.wizard.tree.editor;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXCollapsiblePane;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.editor.ExpressionEditorView;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTree2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreeModel2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreePanel;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.utils.Pair;

public class DomainTreeEditorView<T extends AbstractEntity> extends BasePanel {

    private static final long serialVersionUID = 268187881676011630L;

    private final DomainTreeEditorModel<T> domainTreeEditorModel;

    //    private final JLabel treeCaption;

    public DomainTreeEditorView(final DomainTreeEditorModel<T> domainTreeEditorModel){
	super(new MigLayout("fill, insets 0", "[fill, grow]", "[][fill, grow][]"));
	this.domainTreeEditorModel = domainTreeEditorModel;


	//Configuring the property management action panel.
	final JToolBar toolBar = domainTreeEditorModel.getPropertyManagementActionPanel().buildActionPanel();
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEmptyBorder());
	add(toolBar, "wrap");

	//Configuring the entities tree.
	final EntitiesTreeModel2 treeModel = domainTreeEditorModel.createTreeModel();
	final EntitiesTree2 tree = new EntitiesTree2(treeModel);
	tree.addMouseListener(createPropertyChosenListener(tree));
	tree.getSelectionModel().addTreeSelectionListener(createCalculatedPropertySelectionListener(tree));
	final EntitiesTreePanel treePanel = new EntitiesTreePanel(tree);
	add(treePanel, "wrap");

	//Configuring the expression editor.
	final ExpressionEditorView editorView = new ExpressionEditorView(domainTreeEditorModel.getExpressionModel());
	final JXCollapsiblePane editorPanel = new JXCollapsiblePane(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	editorPanel.setCollapsed(true);
	editorPanel.add(editorView);
	domainTreeEditorModel.addPropertyEditListener(createPropertyEditListener(editorPanel, tree));
	add(editorPanel);
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

    private IPropertyEditListener createPropertyEditListener(final JXCollapsiblePane editorPanel, final EntitiesTree2 tree) {
	return new IPropertyEditListener() {

	    @Override
	    public void startEdit() {
		tree.setEditable(false);
		editorPanel.setCollapsed(false);
	    }

	    @Override
	    public void finishEdit() {
		tree.setEditable(true);
		editorPanel.setCollapsed(true);
	    }
	};
    }

    private TreeSelectionListener createCalculatedPropertySelectionListener(final JTree tree) {
	return new TreeSelectionListener() {

	    @Override
	    public void valueChanged(final TreeSelectionEvent e) {
		final boolean isSeleted = tree.getSelectionModel().isPathSelected(e.getPath());
		getModel().getPropertySelectionModel().propertyStateChanged(getPropertyName(e.getPath()), isSeleted);
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
		    getModel().getExpressionModel().getPropertySelectionModel().propertyStateChanged(getPropertyName(path),true);
		}
	    }
	};
    }

    @SuppressWarnings("unchecked")
    private String getPropertyName(final TreePath path){
	final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
	final Pair<Class<?>, String> rootAndProp = (Pair<Class<?>, String>) node.getUserObject();
	return rootAndProp.getValue();
    }

}
