package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class AbstractExpRightSideConditionalOperand<T, ET extends AbstractEntity<?>> extends AbstractRightSideOperand<T, ET> implements IComparisonQuantifiedOperand<T, ET> {

	@Override
    public IExprOperand0<T, ET> beginExpr() {
    	return copy(new ExprOperand0<T, ET>(){

			@Override
			T getParent3() {
				return AbstractExpRightSideConditionalOperand.this.getParent();
			}
        	
        }, getTokens().beginExpression());
    }
}