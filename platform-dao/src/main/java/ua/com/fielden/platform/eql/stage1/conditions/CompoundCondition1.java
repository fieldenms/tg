package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.utils.ToString;

public record CompoundCondition1(LogicalOperator logicalOperator, ICondition1<? extends ICondition2<?>> condition)
        implements ToString.IFormattable
{

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operator", logicalOperator)
                .add("condition", condition)
                .$();
    }

}
