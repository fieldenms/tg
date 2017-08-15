package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

abstract class FunctionYieldedLastArgument<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<T, IYieldExprItem0<T, ET>, ET> implements IFunctionYieldedLastArgument<T, ET> {

	abstract T getParent3();

    @Override
    IYieldExprItem0<T, ET> getParent2() {
    	return new YieldExprItem0<T, ET>(){

			@Override
			T getParent3() {
				return FunctionYieldedLastArgument.this.getParent3();
			}
        	
        };
    }

    @Override
	T getParent() {
		return getParent3();
	}
}