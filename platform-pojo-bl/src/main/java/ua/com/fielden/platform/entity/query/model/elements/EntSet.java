package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EntSet implements ISetOperand{
    private final List<ISingleOperand> operands;

    public EntSet(final List<ISingleOperand> operands) {
	super();
	this.operands = operands;
    }

    @Override
    public Set<String> getPropNames() {
	final Set<String> result = new HashSet<String>();
	for (final ISingleOperand operand : operands) {
	    result.addAll(operand.getPropNames());
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
