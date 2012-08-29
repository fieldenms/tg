package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhereWithoutNesting;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

abstract class AbstractWhereWithoutNesting<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<? extends IWhereWithoutNesting<T1, T2, ET>>, ET extends AbstractEntity<?>> //
extends AbstractConditionalOperand<T1, T2, ET> //
implements IWhereWithoutNesting<T1, T2, ET> {

    AbstractWhereWithoutNesting(final Tokens queryTokens) {
	super(queryTokens);
    }

    public T2 condition(final ConditionModel condition) {
	return copy(getParent2(), getTokens().cond(condition));
    }
}