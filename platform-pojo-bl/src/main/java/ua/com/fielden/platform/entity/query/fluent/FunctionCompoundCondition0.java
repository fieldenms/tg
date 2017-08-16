package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

abstract class FunctionCompoundCondition0<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IFunctionCompoundCondition0<T, ET> {

	protected abstract T nextForFunctionCompoundCondition0();

	@Override
	public ICaseWhenFunctionArgument<T, ET> then() {
		return copy(createCaseWhenFunctionArgument(), getTokens());
	}

	@Override
	public IFunctionWhere0<T, ET> and() {
		return copy(createFunctionWhere0(), getTokens().and());
	}

	@Override
	public IFunctionWhere0<T, ET> or() {
		return copy(createFunctionWhere0(), getTokens().or());
	}
	
	private FunctionWhere0<T, ET> createFunctionWhere0() {
		return new FunctionWhere0<T, ET>() {

			@Override
			protected T nextForFunctionWhere0() {
				return FunctionCompoundCondition0.this.nextForFunctionCompoundCondition0();
			}

		};
	}
	
	private CaseWhenFunctionArgument<T, ET> createCaseWhenFunctionArgument() {
		return new CaseWhenFunctionArgument<T, ET>() {

			@Override
			protected T nextForCaseWhenFunctionArgument() {
				return FunctionCompoundCondition0.this.nextForFunctionCompoundCondition0();
			}

		};
	}
}