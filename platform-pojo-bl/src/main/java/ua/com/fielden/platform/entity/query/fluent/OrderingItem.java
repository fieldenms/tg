package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

class OrderingItem extends AbstractExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> implements IOrderingItem {

    @Override
    ISingleOperandOrderable getParent() {
        return new SingleOperandOrderable();
    }

    @Override
    IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>> getParent2() {
        ISingleOperandOrderable parent = getParent();
    	return new ExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>(){

			@Override
			ISingleOperandOrderable getParent3() {
				return parent;
			}
        	
        };
    }

    @Override
    public ISingleOperandOrderable yield(final String yieldAlias) {
        return copy(new SingleOperandOrderable(), getTokens().yield(yieldAlias));
    }
}