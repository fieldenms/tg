package ua.com.fielden.platform.swing.pivot.analysis.treetable;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import ua.com.fielden.platform.entity.AbstractEntity;

public class PivotTreeTableNode extends AbstractMutableTreeTableNode {

    public final static String NULL_USER_OBJECT = "UNKNOWN";

    private final Map<String, Object> values = new HashMap<String, Object>();
    private final PivotTreeTableModel model;

    public PivotTreeTableNode(final Object userObject, final Map<String, Object> values, final PivotTreeTableModel model) {
	super(userObject);
	this.model = model;
	this.values.putAll(values != null ? values : new HashMap<String, Object>());
    }

    @Override
    public Object getValueAt(final int column) {
	if (column == 0) {
	    if (getUserObject() instanceof AbstractEntity) {
		final AbstractEntity entity = (AbstractEntity) getUserObject();
		return entity.getKey().toString() + " - " + entity.getDesc();
	    }
	    return getUserObject();
	}
	final String alias = model.getAggregationAliasFor(column - 1);
	if (alias != null) {
	    return values.get(alias);
	}
	return null;
    }

    @Override
    public int getColumnCount() {
	return model.getColumnCount();
    }

    public String getTooltipAt(final int column) {
	if (column == 0) {
	    if (getUserObject() instanceof AbstractEntity) {
		return ((AbstractEntity) getUserObject()).getDesc();
	    }
	    return getUserObject().toString();
	}
	final Object value = getValueAt(column);
	return value != null ? value.toString() : null;

    }

    /**
     * Sort children of this node, using specified comparator.
     * 
     * @param treeTableSorter
     */
    public void sort(final Comparator<MutableTreeTableNode> treeTableSorter) {
	for (final MutableTreeTableNode child : children) {
	    if (child instanceof PivotTreeTableNode) {
		((PivotTreeTableNode) child).sort(treeTableSorter);
	    }
	}
	Collections.sort(children, treeTableSorter);
    }

    /**
     * Returns the value for the specified alias.
     * 
     * @param alias
     * @return
     */
    public Object getValueFor(final String alias) {
	return values.get(alias);
    }

    /**
     * Set value for specified alias.
     * 
     * @param alias
     * @param value
     */
    public void setValueFor(final String alias, final Object value) {
	values.put(alias, value);
    }
}
