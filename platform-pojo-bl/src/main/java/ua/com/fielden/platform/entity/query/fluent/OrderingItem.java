package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

class OrderingItem //
		extends ExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> //
		implements IOrderingItem {

    public OrderingItem(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected ISingleOperandOrderable nextForSingleOperand(final Tokens tokens) {
		return new SingleOperandOrderable(tokens);
	}

	@Override
	protected IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>(tokens) {

			@Override
			protected ISingleOperandOrderable nextForExprOperand0(final Tokens tokens) {
				return OrderingItem.this.nextForSingleOperand(tokens);
			}

		};
	}

	@Override
	public ISingleOperandOrderable yield(final String yieldAlias) {
		return new SingleOperandOrderable(getTokens().yield(yieldAlias));
	}
}