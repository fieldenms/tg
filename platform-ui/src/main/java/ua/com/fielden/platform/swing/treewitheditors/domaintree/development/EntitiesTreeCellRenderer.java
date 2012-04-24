package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeCellRenderer2;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreeModel2.EntitiesTreeUserObject;
import ua.com.fielden.platform.utils.EntityUtils;

public class EntitiesTreeCellRenderer extends MultipleCheckboxTreeCellRenderer2 {

    private static final long serialVersionUID = 2940223267761789273L;

    private static final int gapBetweenLabelAndButtonGroup = 10;
    private static final int gapBetweenButtons = 5;
    private static final int buttonWidth =16;
    private static final int buttonHeight =16;

    private final Font originalFont;
    private final Font derivedFont;

    private final ActionImagePanel newPanel;
    private final ActionImagePanel editPanel;
    private final ActionImagePanel copyPanel;
    private final ActionImagePanel removePanel;
    private final List<ActionImagePanel> confPanel;

    public EntitiesTreeCellRenderer(//
	    final EntitiesTreeModel2 model,//
	    final Action newAction,//
	    final Action editAction,//
	    final Action copyAction,//
	    final Action removeAction) {
	super(model);
	//Creating and configuring all action panels
	newPanel = new ActionImagePanel(newAction);
	editPanel = new ActionImagePanel(editAction);
	copyPanel = new ActionImagePanel(copyAction);
	removePanel = new ActionImagePanel(removeAction);

	this.confPanel = new ArrayList<ActionImagePanel>();
	this.confPanel.add(newPanel);
	this.confPanel.add(editPanel);
	this.confPanel.add(copyPanel);
	this.confPanel.add(removePanel);

	setButtonsVisible(false);
	for(final ActionImagePanel actionPanel : confPanel){
	    actionPanel.setFocusable(true);
	}

	originalFont = label.getFont();
	derivedFont = originalFont.deriveFont(Font.BOLD);
	label.setLeafIcon(null);
	label.setClosedIcon(null);
	label.setOpenIcon(null);
    }

    @Override
    public Dimension getPreferredSize() {
	final Dimension superSize = super.getPreferredSize();
	int d_width = superSize.width;
	boolean first = true;
	for(final ActionImagePanel actionPanel : confPanel){
	    if(actionPanel.isVisible()){
		if(first){
		    d_width += gapBetweenLabelAndButtonGroup + buttonWidth;
		    first = false;
		} else {
		    d_width += gapBetweenButtons + buttonWidth;
		}
	    }
	}
	final int d_height = superSize.height < buttonHeight ? buttonHeight : superSize.height;
	return new Dimension(d_width, d_height);
    }

    @Override
    protected void initView() {
	setLayout(new MigLayout("fill, insets 0", "[]0[]0[fill, grow]"+gapBetweenLabelAndButtonGroup+"[]"+gapBetweenButtons+"[]"+gapBetweenButtons+"[]"+gapBetweenButtons+"[]", "[]"));

	for (int modelIndex = 0; modelIndex < model.getCheckingModelCount(); modelIndex++) {
	    add(checkingComponents.get(modelIndex).getComponent());
	}
	add(label);
    }

    /**
     * Set specified visible flag for all configuration buttons : new, edit, copy, delete.
     * 
     * @param visible
     */
    public void setButtonsVisible(final boolean visible){
	for(final ActionImagePanel actionPanel : confPanel){
	    actionPanel.setVisible(visible);
	}
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {

	//Configuring the visibility of checking components.
	setCheckingComponentsVisible(true);

	final EntitiesTreeNode2 node = (EntitiesTreeNode2) value;
	final Class<?> root = node.getUserObject().getKey();
	final String property = node.getUserObject().getValue();

	if (!getModel().isNotDummyAndNotCommonProperty(property)) {
	    setCheckingComponentsVisible(false);
	}

	if (PropertyTypeDeterminator.isDotNotation(property)) {
	    final String parentProperty = PropertyTypeDeterminator.penultAndLast(property).getKey();
	    if (!AbstractDomainTree.isCommonBranch(parentProperty) && EntityUtils.isUnionEntityType(PropertyTypeDeterminator.determinePropertyType(root, AbstractDomainTree.reflectionProperty(parentProperty)))) {
		setCheckingComponentVisible(1, false);
	    }
	}

	// this action should make matched nodes to render bold.
	if (getModel().getFilterableModel().matches((TreeNode) value)) {
	    label.setFont(derivedFont);
	} else {
	    label.setFont(originalFont);
	}

	// "Entity" node distinguishing from property nodes :
	if (node.getLevel() == 1) {
	    label.setFont(label.getFont().deriveFont(label.getFont().getStyle() + Font.ITALIC));
	}

	//Adding visible configuration buttons to this cell renderer component.
	removeButtons();
	for(final ActionImagePanel button : confPanel){
	    if(button.isVisible()){
		add(button ,"width " + buttonWidth + ":" + buttonWidth + ":" + buttonWidth + ", height " + buttonHeight + ":" + buttonHeight + ":" + buttonHeight);
	    }
	}

	return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    /**
     * Removes all configuration buttons from the container.
     */
    private void removeButtons() {
	for(final ActionImagePanel button : confPanel){
	    remove(button);
	}
    }

    /**
     * Returns {@link IDomainTreeManagerAndEnhancer}
     * 
     * @return
     */
    protected IDomainTreeManagerAndEnhancer getManager() {
	return getModel().getManager();
    }

    @Override
    protected String getLabelToolTipText(final TreePath treePath) {
	if (treePath != null) {
	    final EntitiesTreeUserObject userObject = ((EntitiesTreeNode2) treePath.getLastPathComponent()).getUserObject();
	    return userObject.getLabelTooltip();
	}
	return null;
    }

    @Override
    protected String getCheckingComponentToolTipText(final int index, final TreePath treePath) {
	if (treePath != null) {
	    final EntitiesTreeUserObject userObject = ((EntitiesTreeNode2) treePath.getLastPathComponent()).getUserObject();
	    if (index == 0) {
		return userObject.getFirstTickTooltip(); // createCriteriaCheckboxToolTipText(root, property);
	    } else if (index == 1) {
		return userObject.getSecondTickTooltip(); // createResultSetCheckboxToolTipText(root, property);
	    }
	    return super.getCheckingComponentToolTipText(index, treePath);
	}
	return null;
    }

    @Override
    public EntitiesTreeModel2 getModel() {
	return (EntitiesTreeModel2) super.getModel();
    }

    protected void setNewButtonVisible(final boolean visible){
	newPanel.setVisible(visible);
    }

    protected void setEditButtonVisible(final boolean visible){
	editPanel.setVisible(visible);
    }

    protected void setCopyButtonVisible(final boolean visible){
	copyPanel.setVisible(visible);
    }

    protected void setRemoveButtonVisible(final boolean visible){
	removePanel.setVisible(visible);
    }
}
