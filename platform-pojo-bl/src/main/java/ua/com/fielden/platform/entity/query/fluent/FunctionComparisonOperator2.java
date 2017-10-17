package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;

abstract class FunctionComparisonOperator2<T, ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IFunctionCompoundCondition2<T, ET>, ET> //
		implements IFunctionComparisonOperator2<T, ET> {

    protected FunctionComparisonOperator2(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionComparisonOperator2(final Tokens tokens);

	@Override
	protected IFunctionCompoundCondition2<T, ET> nextForComparisonOperator(final Tokens tokens) {
		return new FunctionCompoundCondition2<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition2(final Tokens tokens) {
				return FunctionComparisonOperator2.this.nextForFunctionComparisonOperator2(tokens);
			}

		};
	}
}