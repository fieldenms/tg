package ua.com.fielden.platform.eql.stage1.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;

public class CompoundCondition1 {
    public final LogicalOperator logicalOperator;
    public final ICondition1<? extends ICondition2<?>> condition;

    public CompoundCondition1(final LogicalOperator logicalOperator, final ICondition1<? extends ICondition2<?>> condition) {
        this.logicalOperator = logicalOperator;
        this.condition = condition;
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
        
        if (!(obj instanceof CompoundCondition1)) {
            return false;
        }
        
        final CompoundCondition1 other = (CompoundCondition1) obj;

        return Objects.equals(logicalOperator, other.logicalOperator) &&
                Objects.equals(condition, other.condition);
    }
}
