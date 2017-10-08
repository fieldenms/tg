package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionCompoundCondition3<T, ET extends AbstractEntity<?>> //
		extends CompoundCondition<IFunctionWhere3<T, ET>, IFunctionCompoundCondition2<T, ET>> //
		implements IFunctionCompoundCondition3<T, ET> {

    protected FunctionCompoundCondition3(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionCompoundCondition3(final Tokens tokens);

	@Override
	protected IFunctionWhere3<T, ET> nextForLogicalCondition(final Tokens tokens) {
		return new FunctionWhere3<T, ET>(tokens) {

			@Override
			protected T nextForFunctionWhere3(final Tokens tokens) {
				return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3(tokens);
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition2<T, ET> nextForCompoundCondition(final Tokens tokens) {
		return new FunctionCompoundCondition2<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition2(final Tokens tokens) {
				return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3(tokens);
			}

		};
	}
}