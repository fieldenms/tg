package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

abstract class FunctionYieldedLastArgument<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<T, IYieldExprItem0<T, ET>, ET> implements IFunctionYieldedLastArgument<T, ET> {

	abstract T nextForFunctionYieldedLastArgument();

    @Override
    IYieldExprItem0<T, ET> nextForAbstractYieldExprOperand() {
    	return new YieldExprItem0<T, ET>(){

			@Override
			T nextForYieldExprItem0() {
				return FunctionYieldedLastArgument.this.nextForFunctionYieldedLastArgument();
			}
        	
        };
    }

    @Override
	T nextForAbstractSingleOperand() {
		return nextForFunctionYieldedLastArgument();
	}
}