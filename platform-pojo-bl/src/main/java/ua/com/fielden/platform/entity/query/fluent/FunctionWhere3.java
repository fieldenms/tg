package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionWhere3<T, ET extends AbstractEntity<?>> //
		extends ConditionalOperand<IFunctionComparisonOperator3<T, ET>, IFunctionCompoundCondition3<T, ET>, ET> //
		implements IFunctionWhere3<T, ET> {

    protected FunctionWhere3(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionWhere3(final Tokens tokens);

	@Override
	protected IFunctionCompoundCondition3<T, ET> nextForConditionalOperand(final Tokens tokens) {
		return new FunctionCompoundCondition3<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition3(final Tokens tokens) {
				return FunctionWhere3.this.nextForFunctionWhere3(tokens);
			}

		};
	}

	@Override
	protected IFunctionComparisonOperator3<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new FunctionComparisonOperator3<T, ET>(tokens) {

			@Override
			protected T nextForFunctionComparisonOperator3(final Tokens tokens) {
				return FunctionWhere3.this.nextForFunctionWhere3(tokens);
			}

		};
	}
}