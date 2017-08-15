package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

abstract class FunctionWhere0<T, ET extends AbstractEntity<?>> extends AbstractWhere<IFunctionComparisonOperator0<T, ET>, IFunctionCompoundCondition0<T, ET>, IFunctionWhere1<T, ET>, ET> implements IFunctionWhere0<T, ET> {

	abstract T nextForFunctionWhere0();

    @Override
    protected IFunctionWhere1<T, ET> nextForAbstractWhere() {
    	return new FunctionWhere1<T, ET>(){

			@Override
			T nextForFunctionWhere1() {
				return FunctionWhere0.this.nextForFunctionWhere0();
			}
        	
        };
    }

    @Override
    IFunctionCompoundCondition0<T, ET> nextForAbstractConditionalOperand() {
        return new FunctionCompoundCondition0<T, ET>(){

			@Override
			T nextForFunctionCompoundCondition0() {
				return FunctionWhere0.this.nextForFunctionWhere0();
			}
        	
        };
    }

    @Override
    IFunctionComparisonOperator0<T, ET> nextForAbstractSingleOperand() {
        return new FunctionComparisonOperator0<T, ET>(){

			@Override
			T nextForFunctionComparisonOperator0() {
				return FunctionWhere0.this.nextForFunctionWhere0();
			}};
    }
}