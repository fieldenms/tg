package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

class OrderingItem extends AbstractExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> implements IOrderingItem {

    @Override
    ISingleOperandOrderable nextForAbstractSingleOperand() {
        return new SingleOperandOrderable();
    }

    @Override
    IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>> nextForAbstractExprOperand() {
    	return new ExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>(){

			@Override
			ISingleOperandOrderable nextForExprOperand0() {
				return OrderingItem.this.nextForAbstractSingleOperand();
			}
        	
        };
    }

    @Override
    public ISingleOperandOrderable yield(final String yieldAlias) {
        return copy(new SingleOperandOrderable(), getTokens().yield(yieldAlias));
    }
}