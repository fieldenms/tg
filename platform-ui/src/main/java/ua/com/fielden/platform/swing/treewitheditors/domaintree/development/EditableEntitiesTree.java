package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import javax.swing.Action;

/**
 * The {@link EntitiesTree2} with ability to add/edit/copy/remove calculated property.
 *
 * @author TG Team
 *
 */
public class EditableEntitiesTree extends EntitiesTree2 {

    private static final long serialVersionUID = -4085856123677198967L;

    /**
     * Initialises this {@link EditableEntitiesTree} with appropriate {@link EntitiesTreeModel2} instance and new/edit/copy/remove actions for cell editor.
     *
     * @param entitiesTreeModel2
     * @param newAction
     * @param editAction
     * @param copyAction
     * @param removeAction
     */
    public EditableEntitiesTree(//
	    final EntitiesTreeModel2 entitiesTreeModel2,//
	    final Action newAction,//
	    final Action editAction,//
	    final Action copyAction,//
	    final Action removeAction) {
	super(entitiesTreeModel2);

	final EntitiesTreeCellRenderer cellRenderer1, cellRenderer2;

	cellRenderer1 = new EntitiesTreeCellRenderer(entitiesTreeModel2, //
	newAction, editAction, copyAction, removeAction);
	cellRenderer2 = new EntitiesTreeCellRenderer(entitiesTreeModel2, //
	newAction, editAction, copyAction, removeAction);

	setCellRenderer(cellRenderer1);
	setCellEditor(new EntitiesTreeCellEditor(this, cellRenderer2));
    }

}
