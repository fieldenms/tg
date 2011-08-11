package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.ISingleOperand;

public class Expression implements ISingleOperand {

    public Expression(final ISingleOperand first, final List<CompoundSingleOperand> items) {
	super();
	this.first = first;
	this.items = items;
    }

    private final ISingleOperand first;

    private final List<CompoundSingleOperand> items;

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
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof Expression))
	    return false;
	final Expression other = (Expression) obj;
	if (first == null) {
	    if (other.first != null)
		return false;
	} else if (!first.equals(other.first))
	    return false;
	if (items == null) {
	    if (other.items != null)
		return false;
	} else if (!items.equals(other.items))
	    return false;
	return true;
    }

    @Override
    public List<String> getPropNames() {
	final List<String> result = new ArrayList<String>();
	result.addAll(first.getPropNames());
	for (final CompoundSingleOperand compSingleOperand : items) {
	    result.addAll(compSingleOperand.getOperand().getPropNames());
	}
	return result;
    }
}
