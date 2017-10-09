package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

abstract class FunctionCompoundCondition0<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IFunctionCompoundCondition0<T, ET> {

    protected FunctionCompoundCondition0(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionCompoundCondition0(final Tokens tokens);

	@Override
	public ICaseWhenFunctionArgument<T, ET> then() {
		return createCaseWhenFunctionArgument(getTokens());
	}

	@Override
	public IFunctionWhere0<T, ET> and() {
		return createFunctionWhere0(getTokens().and());
	}

	@Override
	public IFunctionWhere0<T, ET> or() {
		return createFunctionWhere0(getTokens().or());
	}
	
	private FunctionWhere0<T, ET> createFunctionWhere0(final Tokens tokens) {
		return new FunctionWhere0<T, ET>(tokens) {

			@Override
			protected T nextForFunctionWhere0(final Tokens tokens) {
				return FunctionCompoundCondition0.this.nextForFunctionCompoundCondition0(tokens);
			}

		};
	}
	
	private CaseWhenFunctionArgument<T, ET> createCaseWhenFunctionArgument(final Tokens tokens) {
		return new CaseWhenFunctionArgument<T, ET>(tokens) {

			@Override
			protected T nextForCaseWhenFunctionArgument(final Tokens tokens) {
				return FunctionCompoundCondition0.this.nextForFunctionCompoundCondition0(tokens);
			}

		};
	}
}