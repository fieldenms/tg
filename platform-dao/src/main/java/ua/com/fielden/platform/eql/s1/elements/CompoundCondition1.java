package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;

public class CompoundCondition1 {
    private final LogicalOperator logicalOperator;
    private final ICondition1<? extends ICondition2> condition;

    //    public String sql() {
    //	return " " + logicalOperator + " " + condition.sql();
    //    }

    public CompoundCondition1(final LogicalOperator logicalOperator, final ICondition1<? extends ICondition2> condition) {
        this.logicalOperator = logicalOperator;
        this.condition = condition;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public ICondition1<? extends ICondition2> getCondition() {
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
        if (!(obj instanceof CompoundCondition1)) {
            return false;
        }
        final CompoundCondition1 other = (CompoundCondition1) obj;
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
