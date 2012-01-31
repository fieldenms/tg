package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class EntSet implements ISetOperand{
    private final List<ISingleOperand> operands;

    @Override
    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append("(");
	for (final Iterator<ISingleOperand> iterator = operands.iterator(); iterator.hasNext();) {
	    final ISingleOperand operand = iterator.next();
	    sb.append(operand.sql());
	    if (iterator.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(")");
	return sb.toString();
    }

    public EntSet(final List<ISingleOperand> operands) {
	super();
	this.operands = operands;
    }

    @Override
    public List<EntProp> getProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getProps());
	}
	return result;
    }

    @Override
    public List<EntQuery> getSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getSubqueries());
	}
	return result;
    }

    @Override
    public List<EntValue> getValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getValues());
	}
	return result;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((operands == null) ? 0 : operands.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof EntSet)) {
	    return false;
	}
	final EntSet other = (EntSet) obj;
	if (operands == null) {
	    if (other.operands != null) {
		return false;
	    }
	} else if (!operands.equals(other.operands)) {
	    return false;
	}
	return true;
    }
}