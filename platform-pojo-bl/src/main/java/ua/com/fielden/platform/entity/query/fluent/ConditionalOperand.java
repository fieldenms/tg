package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExistenceOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.model.QueryModel;

abstract class ConditionalOperand<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
		extends ExpConditionalOperand<T1, ET> //
		implements IComparisonOperand<T1, ET>, IExistenceOperator<T2> {

    protected ConditionalOperand(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T2 nextForConditionalOperand(final Tokens tokens);

	@Override
	public T2 exists(final QueryModel subQuery) {
		return nextForConditionalOperand(getTokens().exists(false, subQuery));
	}

	@Override
	public T2 notExists(final QueryModel subQuery) {
		return nextForConditionalOperand(getTokens().exists(true, subQuery));
	}

	@Override
	public T2 existsAnyOf(final QueryModel... subQueries) {
		return nextForConditionalOperand(getTokens().existsAnyOf(false, subQueries));
	}

	@Override
	public T2 notExistsAnyOf(final QueryModel... subQueries) {
		return nextForConditionalOperand(getTokens().existsAnyOf(true, subQueries));
	}

	@Override
	public T2 existsAllOf(final QueryModel... subQueries) {
		return nextForConditionalOperand(getTokens().existsAllOf(false, subQueries));
	}

	@Override
	public T2 notExistsAllOf(final QueryModel... subQueries) {
		return nextForConditionalOperand(getTokens().existsAllOf(true, subQueries));
	}
}