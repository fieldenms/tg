package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.menu.filter.IFilterListener;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.treewitheditors.development.MultipleCheckboxTreeCellEditor2;
import ua.com.fielden.platform.utils.EntityUtils;

public class EntitiesTreeCellEditor extends MultipleCheckboxTreeCellEditor2 {

    private static final long serialVersionUID = -9095582803874450838L;

    public EntitiesTreeCellEditor(final EntitiesTree2<ICentreDomainTreeManagerAndEnhancer> tree, final EntitiesTreeCellRenderer renderer) {
        super(tree, renderer);

        tree.getEntitiesModel().getFilterableModel().addFilterListener(new IFilterListener() {

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
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
        final EntitiesTreeNode2<ICentreDomainTreeManagerAndEnhancer> node = (EntitiesTreeNode2<ICentreDomainTreeManagerAndEnhancer>) value;
        getRenderer().setButtonsVisible(false);
        try {
            getTree().getEntitiesModel().getManager().getEnhancer().getCalculatedProperty(node.getUserObject().getKey(), node.getUserObject().getValue());
            getRenderer().setEditButtonVisible(true);
            getRenderer().setCopyButtonVisible(true);
            getRenderer().setRemoveButtonVisible(true);

        } catch (final IncorrectCalcPropertyException ex) {
            if (EntityUtils.isEntityType(propertyType(node))) {
                getRenderer().setNewButtonVisible(true);
            }
        }
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntitiesTree2<ICentreDomainTreeManagerAndEnhancer> getTree() {
        return (EntitiesTree2<ICentreDomainTreeManagerAndEnhancer>) super.getTree();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getCellEditorValue() {
        return ((EntitiesTreeNode2<ICentreDomainTreeManagerAndEnhancer>) getTree().getEditingPath().getLastPathComponent()).getUserObject();
    }

    @Override
    public EntitiesTreeCellRenderer getRenderer() {
        return (EntitiesTreeCellRenderer) super.getRenderer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isCellEditable(final EventObject event) {
        if (super.isCellEditable(event)) {
            return true;
        }
        if (event instanceof MouseEvent) {
            final MouseEvent e = (MouseEvent) event;
            if (!e.isControlDown() && !e.isConsumed()) {
                final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return false;
                }
                final Object lastComponent = path.getLastPathComponent();
                final EntitiesTreeNode2<ICentreDomainTreeManagerAndEnhancer> node = (EntitiesTreeNode2<ICentreDomainTreeManagerAndEnhancer>) lastComponent;
                try {
                    getTree().getEntitiesModel().getManager().getEnhancer().getCalculatedProperty(node.getUserObject().getKey(), node.getUserObject().getValue());
                    return true;
                } catch (final IncorrectCalcPropertyException ex) {
                    if (EntityUtils.isEntityType(propertyType(node))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected Class<?> propertyType(final EntitiesTreeNode2<ICentreDomainTreeManagerAndEnhancer> node) {
        final Class<?> enhancedRoot = getTree().getEntitiesModel().getManager().getEnhancer().getManagedType(node.getUserObject().getKey());
        final String property = node.getUserObject().getValue();
        final Class<?> propertyType = StringUtils.isEmpty(property) ? enhancedRoot : PropertyTypeDeterminator.determinePropertyType(enhancedRoot, property);
        return propertyType;
    }
}
