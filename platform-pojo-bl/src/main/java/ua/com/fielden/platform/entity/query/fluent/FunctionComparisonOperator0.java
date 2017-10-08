package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;

abstract class FunctionComparisonOperator0<T, ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IFunctionCompoundCondition0<T, ET>, ET> //
		implements IFunctionComparisonOperator0<T, ET> {

    protected FunctionComparisonOperator0(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionComparisonOperator0(final Tokens tokens);

	@Override
	protected IFunctionCompoundCondition0<T, ET> nextForComparisonOperator(final Tokens tokens) {
		return new FunctionCompoundCondition0<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition0(final Tokens tokens) {
				return FunctionComparisonOperator0.this.nextForFunctionComparisonOperator0(tokens);
			}

		};
	}
}