package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;

public record CompoundCondition1(LogicalOperator logicalOperator, ICondition1<? extends ICondition2<?>> condition) {

}
