package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperationOrEnd3<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IExprOperand3<T, ET>, IExprOperationOrEnd2<T, ET>, ET> implements IExprOperationOrEnd3<T, ET> {

	abstract T getParent3();

    @Override
    IExprOperationOrEnd2<T, ET> getParent2() {
    	return new ExprOperationOrEnd2<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperationOrEnd3.this.getParent3();
			}
        	
        };
    }

    @Override
    IExprOperand3<T, ET> getParent() {
    	return new ExprOperand3<T, ET>(){

			@Override
			T getParent2() {
				return ExprOperationOrEnd3.this.getParent3();
			}};
    }
}