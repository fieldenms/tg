package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;

abstract class AbstractExpConditionalOperand<T, ET extends AbstractEntity<?>> extends AbstractMultipleOperand<T, ET> implements IComparisonOperand<T, ET> {

    @Override
    public EntityQueryProgressiveInterfaces.IExprOperand0<T, ET> beginExpr() {
    	return copy(new ExprOperand0<T, ET>(){

			@Override
			T getParent3() {
				return AbstractExpConditionalOperand.this.getParent();
			}
        	
        }, getTokens().beginExpression());
    }
}