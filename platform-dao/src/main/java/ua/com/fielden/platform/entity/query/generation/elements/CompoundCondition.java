package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;



public class CompoundCondition {
    private final LogicalOperator logicalOperator;
    private final ICondition condition;

    public String sql() {
	return " " + logicalOperator + " " + condition.sql();
    }

    public CompoundCondition(final LogicalOperator logicalOperator, final ICondition condition) {
	this.logicalOperator = logicalOperator;
	this.condition = condition;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public ICondition getCondition() {
        return condition;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((condition == null) ? 0 : condition.hashCode());
	result = prime * result + ((logicalOperator == null) ? 0 : logicalOperator.hashCode());
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
	if (!(obj instanceof CompoundCondition)) {
	    return false;
	}
	final CompoundCondition other = (CompoundCondition) obj;
	if (condition == null) {
	    if (other.condition != null) {
		return false;
	    }
	} else if (!condition.equals(other.condition)) {
	    return false;
	}
	if (logicalOperator != other.logicalOperator) {
	    return false;
	}
	return true;
    }
}
