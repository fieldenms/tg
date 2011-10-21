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
import ua.com.fielden.platform.swing.dynamicreportstree.CriteriaTree;
import ua.com.fielden.platform.swing.dynamicreportstree.TreePanel;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.treemodel.CriteriaTreeModel;

public class DomainTreeEditorView<T extends AbstractEntity> extends BasePanel {

    private static final long serialVersionUID = 268187881676011630L;

    private final DomainTreeEditorModel<T> domainTreeEditorModel;

    //    private final JLabel treeCaption;

    public DomainTreeEditorView(final DomainTreeEditorModel<T> wizardModel){
	super(new MigLayout("fill, insets 0", "[fill, grow]", "[][fill, grow][]"));
	this.domainTreeEditorModel = wizardModel;


	//Configuring the property management action panel.
	final JToolBar toolBar = wizardModel.getPropertyManagementActionPanel().buildActionPanel();
	toolBar.setFloatable(false);
	toolBar.setBorder(BorderFactory.createEmptyBorder());
	add(toolBar, "wrap");

	//Configuring the entities tree.
	final CriteriaTreeModel treeModel = wizardModel.createTreeModel();
	final CriteriaTree tree = new CriteriaTree(treeModel);
	tree.addMouseListener(createPropertyChosenListener(tree, treeModel));
	tree.getSelectionModel().addTreeSelectionListener(createCalculatedPropertySelectionListener(tree, treeModel));
	final TreePanel treePanel = new TreePanel(tree);
	add(treePanel, "wrap");

	//Configuring the expression editor.
	final ExpressionEditorView editorView = new ExpressionEditorView(wizardModel.getExpressionModel());
	final JXCollapsiblePane editorPanel = new JXCollapsiblePane(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	editorPanel.setCollapsed(true);
	editorPanel.add(editorView);
	wizardModel.addPropertyEditListener(createPropertyEditListener(editorPanel, tree));
	add(editorPanel);
    }

    private IPropertyEditListener createPropertyEditListener(final JXCollapsiblePane editorPanel, final CriteriaTree tree) {
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

    private TreeSelectionListener createCalculatedPropertySelectionListener(final JTree tree, final CriteriaTreeModel treeModel) {
	return new TreeSelectionListener() {

	    @Override
	    public void valueChanged(final TreeSelectionEvent e) {
		final TreePath path = e.getPath();
		final String propertyName = treeModel.getPropertyNameFor((DefaultMutableTreeNode)path.getLastPathComponent());
		final boolean isSeleted = tree.getSelectionModel().isPathSelected(path);
		getModel().getPropertySelectionModel().propertyStateChanged(propertyName, isSeleted);
	    }
	};
    }

    private MouseListener createPropertyChosenListener(final JTree tree, final CriteriaTreeModel treeModel) {
	return new MouseAdapter() {
	    @Override
	    public void mousePressed(final MouseEvent e) {
		final int x = e.getX();
		final int y = e.getY();
		final TreePath path = tree.getPathForLocation(x, y);
		if (path != null && getModel().getExpressionModel().getState() != UmState.VIEW) {
		    getModel().getExpressionModel().getPropertySelectionModel().propertyStateChanged(treeModel.getPropertyNameFor((DefaultMutableTreeNode)path.getLastPathComponent()),true);
		}
	    }
	};
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

}
