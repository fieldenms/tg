package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;


public class Expression implements ISingleOperand {

    private final ISingleOperand first;
    private final List<CompoundSingleOperand> items;

    @Override
    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append("(" + first.sql());
	for (final CompoundSingleOperand compoundOperand : items) {
	    sb.append(compoundOperand.sql());
	}
	sb.append(")");

	return sb.toString();
    }


    public Expression(final ISingleOperand first, final List<CompoundSingleOperand> items) {
	super();
	this.first = first;
	this.items = items;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	result.addAll(first.getLocalProps());
	for (final CompoundSingleOperand compSingleOperand : items) {
	    result.addAll(compSingleOperand.getOperand().getLocalProps());
	}
	return result;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	result.addAll(first.getLocalSubQueries());
	for (final CompoundSingleOperand compSingleOperand : items) {
	    result.addAll(compSingleOperand.getOperand().getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	result.addAll(first.getAllValues());
	for (final CompoundSingleOperand compSingleOperand : items) {
	    result.addAll(compSingleOperand.getOperand().getAllValues());
	}
	return result;
    }

    @Override
    public Class type() {
	return null;
    }

    @Override
    public Object hibType() {
	return null;
    }

    @Override
    public boolean isNullable() {
	return true;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((first == null) ? 0 : first.hashCode());
	result = prime * result + ((items == null) ? 0 : items.hashCode());
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
	if (!(obj instanceof Expression)) {
	    return false;
	}
	final Expression other = (Expression) obj;
	if (first == null) {
	    if (other.first != null) {
		return false;
	    }
	} else if (!first.equals(other.first)) {
	    return false;
	}
	if (items == null) {
	    if (other.items != null) {
		return false;
	    }
	} else if (!items.equals(other.items)) {
	    return false;
	}
	return true;
    }
}