package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

abstract class FunctionCompoundCondition2<T, ET extends AbstractEntity<?>> //
		extends CompoundCondition<IFunctionWhere2<T, ET>, IFunctionCompoundCondition1<T, ET>> //
		implements IFunctionCompoundCondition2<T, ET> {

    protected FunctionCompoundCondition2(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionCompoundCondition2(final Tokens tokens);

	@Override
	protected IFunctionWhere2<T, ET> nextForLogicalCondition(final Tokens tokens) {
		return new FunctionWhere2<T, ET>(tokens) {

			@Override
			protected T nextForFunctionWhere2(final Tokens tokens) {
				return FunctionCompoundCondition2.this.nextForFunctionCompoundCondition2(tokens);
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition1<T, ET> nextForCompoundCondition(final Tokens tokens) {
		return new FunctionCompoundCondition1<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition1(final Tokens tokens) {
				return FunctionCompoundCondition2.this.nextForFunctionCompoundCondition2(tokens);
			}

		};
	}
}