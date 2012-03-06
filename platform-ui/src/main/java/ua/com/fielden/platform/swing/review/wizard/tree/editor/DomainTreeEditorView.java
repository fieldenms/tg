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

    final JXCollapsiblePane editorPanel;
    //    private final JLabel treeCaption;

    public DomainTreeEditorView(final DomainTreeEditorModel<T> domainTreeEditorModel){
	super(new MigLayout("fill, insets 0", "[fill, grow]", "[fill, grow][fill]"));
	this.domainTreeEditorModel = domainTreeEditorModel;

	//Configuring the entities tree.
	final EntitiesTreeModel2 treeModel = domainTreeEditorModel.createTreeModel();
	final EntitiesTree2 tree = new EntitiesTree2(treeModel);
	tree.addMouseListener(createPropertyChosenListener(tree));
	tree.getSelectionModel().addTreeSelectionListener(createCalculatedPropertySelectionListener(tree));
	final EntitiesTreePanel treePanel = new EntitiesTreePanel(tree);
	add(treePanel, "wrap");

	//Configuring the expression editor.
	final ExpressionEditorView editorView = new ExpressionEditorView(domainTreeEditorModel.getExpressionModel());
	editorPanel = new JXCollapsiblePane(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	editorPanel.setCollapsed(true);
	editorPanel.add(editorView);
	domainTreeEditorModel.addPropertyEditListener(createPropertyEditListener(editorPanel, tree));
	add(editorPanel);
    }

    //TODO Implement this as the task for the ticket #347
    //    /**
    //     * Set the specified animate flag for the collapsible editor panel.
    //     *
    //     * @param animate
    //     */
    //    public void setEditorPanelAnimated(final boolean animate){
    //	editorPanel.setAnimated(animate);
    //    }

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
		//TODO consider whether to start editing tree path after editor panel was collapsed!
		//tree.startEditingAtPath(tree.getSelectionPath());
		editorPanel.setCollapsed(true);
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
