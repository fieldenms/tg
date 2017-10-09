package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;

abstract class FunctionComparisonOperator1<T, ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IFunctionCompoundCondition1<T, ET>, ET> //
		implements IFunctionComparisonOperator1<T, ET> {

    protected FunctionComparisonOperator1(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionComparisonOperator1(final Tokens tokens);

	@Override
	protected IFunctionCompoundCondition1<T, ET> nextForComparisonOperator(final Tokens tokens) {
		return new FunctionCompoundCondition1<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition1(final Tokens tokens) {
				return FunctionComparisonOperator1.this.nextForFunctionComparisonOperator1(tokens);
			}

		};
	}
}