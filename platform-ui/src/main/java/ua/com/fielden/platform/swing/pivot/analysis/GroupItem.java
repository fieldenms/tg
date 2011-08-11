package ua.com.fielden.platform.swing.pivot.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.swing.pivot.analysis.treetable.PivotTreeTableModel;
import ua.com.fielden.platform.swing.pivot.analysis.treetable.PivotTreeTableNode;

public class GroupItem {
    private final Map<Object, GroupItem> children = new HashMap<Object, GroupItem>();
    private final List<IBindingEntity> entities = new ArrayList<IBindingEntity>();
    private final Map<String, Object> calculatedProperties = new HashMap<String, Object>();

    public GroupItem getChild(final Object key) {
	return children.get(key);
    }

    public void putChild(final Object key, final GroupItem child) {
	children.put(key, child);
    }

    public Collection<GroupItem> getChildren() {
	return children.values();
    }

    public void clearChildren() {
	children.clear();
    }

    public void clearEntities() {
	entities.clear();
    }

    public void clearCalculation() {
	calculatedProperties.clear();
    }

    public void addEntity(final IBindingEntity entity) {
	entities.add(entity);
    }

    public List<IBindingEntity> getEntities() {
	final List<IBindingEntity> totalEntities = new ArrayList<IBindingEntity>();
	totalEntities.addAll(entities);
	for (final GroupItem item : children.values()) {
	    totalEntities.addAll(item.getEntities());
	}
	return totalEntities;
    }

    public void calculate(final String alias, final ITotalFunction function) {
	final Collection<GroupItem> groupItems = children.values();
	for (final GroupItem groupItem : groupItems) {
	    groupItem.calculate(alias, function);
	}
	calculatedProperties.put(alias, function.calculateProperty(this));
    }

    public Object getValueFor(final String alias) {
	return calculatedProperties.get(alias);
    }

    public PivotTreeTableNode createTree(final Object key, final PivotTreeTableModel model) {
	final PivotTreeTableNode rootNode = new PivotTreeTableNode(key, calculatedProperties, model);
	for (final Map.Entry<Object, GroupItem> entry : children.entrySet()) {
	    rootNode.add(entry.getValue().createTree(entry.getKey(), model));
	}
	return rootNode;
    }

    public void updateTree(final PivotTreeTableNode root, final String alias) {
	if (root == null) {
	    return;
	}
	root.setValueFor(alias, getValueFor(alias));
	for (final Object key : children.keySet()) {
	    children.get(key).updateTree(getChildFor(key, root), alias);
	}
    }

    private PivotTreeTableNode getChildFor(final Object key, final PivotTreeTableNode root) {
	for (int index = 0; index < root.getChildCount(); index++) {
	    final PivotTreeTableNode childNode = (PivotTreeTableNode) root.getChildAt(index);
	    if (childNode.getUserObject() == key || (childNode.getUserObject() != null && childNode.getUserObject().equals(key))) {
		return childNode;
	    }
	}
	return null;
    }
}
