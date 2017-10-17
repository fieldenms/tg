package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

abstract class FunctionCompoundCondition1<T, ET extends AbstractEntity<?>> //
		extends CompoundCondition<IFunctionWhere1<T, ET>, IFunctionCompoundCondition0<T, ET>> //
		implements IFunctionCompoundCondition1<T, ET> {

    protected FunctionCompoundCondition1(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionCompoundCondition1(final Tokens tokens);

	@Override
	protected IFunctionWhere1<T, ET> nextForLogicalCondition(final Tokens tokens) {
		return new FunctionWhere1<T, ET>(tokens) {

			@Override
			protected T nextForFunctionWhere1(final Tokens tokens) {
				return FunctionCompoundCondition1.this.nextForFunctionCompoundCondition1(tokens);
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition0<T, ET> nextForCompoundCondition(final Tokens tokens) {
		return new FunctionCompoundCondition0<T, ET>(tokens) {

			@Override
			protected T nextForFunctionCompoundCondition0(final Tokens tokens) {
				return FunctionCompoundCondition1.this.nextForFunctionCompoundCondition1(tokens);
			}

		};
	}
}