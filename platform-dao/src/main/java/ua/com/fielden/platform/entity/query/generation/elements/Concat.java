package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;


public class Concat implements ISingleOperand {

    private final List<ISingleOperand> operands;

    public Concat(final List<ISingleOperand> operands) {
	super();
	this.operands = operands;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getLocalProps());
	}
	return result;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getAllValues());
	}
	return result;
    }

    @Override
    public Class type() {
	return String.class;
    }

    @Override
    public Object hibType() {
	return Hibernate.STRING;
    }

    @Override
    public boolean isNullable() {
	return true;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append("CONCAT (");

	for (final Iterator<ISingleOperand> iterator = operands.iterator(); iterator.hasNext();) {
	    final ISingleOperand operand = iterator.next();
	    sb.append(operand.sql());
	    if (iterator.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(")");

	return  sb.toString();
    }

    public List<ISingleOperand> getOperands() {
        return operands;
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
	if (!(obj instanceof Concat)) {
	    return false;
	}
	final Concat other = (Concat) obj;
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