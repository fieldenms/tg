package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

abstract class FunctionCompoundCondition0<T, ET extends AbstractEntity<?>> extends AbstractQueryLink
		implements IFunctionCompoundCondition0<T, ET> {

	abstract T getParent();

	@Override
	public ICaseWhenFunctionArgument<T, ET> then() {
		return copy(new CaseWhenFunctionArgument<T, ET>() {

			@Override
			T getParent3() {
				return FunctionCompoundCondition0.this.getParent();
			}

		}, getTokens());
	}

	@Override
	public IFunctionWhere0<T, ET> and() {
		return copy(new FunctionWhere0<T, ET>() {

			@Override
			T getParent4() {
				return FunctionCompoundCondition0.this.getParent();
			}

		}, getTokens().and());
	}

	@Override
	public IFunctionWhere0<T, ET> or() {
		return copy(new FunctionWhere0<T, ET>() {

			@Override
			T getParent4() {
				return FunctionCompoundCondition0.this.getParent();
			}

		}, getTokens().or());
	}
}