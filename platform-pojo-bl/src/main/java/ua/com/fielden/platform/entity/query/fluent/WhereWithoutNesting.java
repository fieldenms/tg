package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhereWithoutNesting;

abstract class WhereWithoutNesting<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<? extends IWhereWithoutNesting<T1, T2, ET>>, ET extends AbstractEntity<?>> //
		extends ConditionalOperand<T1, T2, ET> //
		implements IWhereWithoutNesting<T1, T2, ET> {

    protected WhereWithoutNesting(final Tokens tokens) {
        super(tokens);
    }
}