package ua.com.fielden.platform.dynamictree;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Data structure needed only for creating fetch models.
 * 
 * @author oleh
 * 
 */
public class DynamicEntityTreeNode {

    /**
     * Map of the children for this {@link DynamicEntityTreeNode}.
     */
    private final Map<String, DynamicEntityTreeNode> children;

    /**
     * Holds information needed to build fetch models.
     */
    private final String name;
    private final Class<?> type;

    /**
     * Parent node for this {@link DynamicEntityTreeNode}.
     */
    private DynamicEntityTreeNode parent;

    public DynamicEntityTreeNode(final String name, final Class<?> type) {
	this.name = name;
	this.type = type;
	children = new HashMap<String, DynamicEntityTreeNode>();

	setParent(null);
    }

    public void addChild(final String key, final DynamicEntityTreeNode treeNode) {
	children.put(key, treeNode);
	treeNode.setParent(this);
    }

    public DynamicEntityTreeNode removeChild(final String key) {
	final DynamicEntityTreeNode treeNode = children.remove(key);
	treeNode.setParent(null);
	return treeNode;
    }

    public DynamicEntityTreeNode getChild(final String key) {
	return children.get(key);
    }

    public Set<String> getKeys() {
	return Collections.unmodifiableSet(children.keySet());
    }

    public int getChildCount() {
	return children.size();
    }

    public String getName() {
	return name;
    }

    public Class<?> getType() {
	return type;
    }

    public Collection<DynamicEntityTreeNode> getChildren() {
	return Collections.unmodifiableCollection(children.values());
    }

    private void setParent(final DynamicEntityTreeNode parent) {
	this.parent = parent;
    }

    public DynamicEntityTreeNode getParent() {
	return parent;
    }

    @Override
    public String toString() {
	return getName();
    }
}
