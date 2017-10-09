package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;

abstract class FunctionComparisonOperator3<T, ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IFunctionCompoundCondition3<T, ET>, ET> //
		implements IFunctionComparisonOperator3<T, ET> {

    protected FunctionComparisonOperator3(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionComparisonOperator3(final Tokens tokens);

	@Override
	protected IFunctionCompoundCondition3<T, ET> nextForComparisonOperator(final Tokens tokens) {
		return new FunctionCompoundCondition3<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition3(final Tokens tokens) {
				return FunctionComparisonOperator3.this.nextForFunctionComparisonOperator3(tokens);
			}

		};
	}
}