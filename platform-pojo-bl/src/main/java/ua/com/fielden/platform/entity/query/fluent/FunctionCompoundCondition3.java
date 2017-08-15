package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionCompoundCondition3<T, ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IFunctionWhere3<T, ET>, IFunctionCompoundCondition2<T, ET>> implements IFunctionCompoundCondition3<T, ET> {

	abstract T nextForFunctionCompoundCondition3();

    @Override
    IFunctionWhere3<T, ET> nextForAbstractLogicalCondition() {
    	return new FunctionWhere3<T, ET>(){

			@Override
			T nextForFunctionWhere3() {
				return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3();
			}
        	
        };
    }

    @Override
    IFunctionCompoundCondition2<T, ET> nextForAbstractCompoundCondition() {
    	return new FunctionCompoundCondition2<T, ET>(){

			@Override
			T nextForFunctionCompoundCondition2() {
				return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3();
			}
        	
        };
    }
}