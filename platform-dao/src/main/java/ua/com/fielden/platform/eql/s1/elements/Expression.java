package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


public class Expression implements ISingleOperand<ua.com.fielden.platform.eql.s2.elements.Expression> {

    private final ISingleOperand<? extends ISingleOperand2> first;
    private final List<CompoundSingleOperand> items;

    public Expression(final ISingleOperand<? extends ISingleOperand2> first, final List<CompoundSingleOperand> items) {
	super();
	this.first = first;
	this.items = items;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.Expression transform(final TransformatorToS2 resolver) {
	final List<ua.com.fielden.platform.eql.s2.elements.CompoundSingleOperand> transformed = new ArrayList<>();
	for (final CompoundSingleOperand item : items) {
	    transformed.add(new ua.com.fielden.platform.eql.s2.elements.CompoundSingleOperand(item.getOperand().transform(resolver), item.getOperator()));
	}
	return new ua.com.fielden.platform.eql.s2.elements.Expression(first.transform(resolver), transformed);
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