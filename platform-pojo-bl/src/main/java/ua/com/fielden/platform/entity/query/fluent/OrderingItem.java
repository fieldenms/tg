package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

class OrderingItem //
		extends ExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> //
		implements IOrderingItem {

	@Override
	protected ISingleOperandOrderable nextForSingleOperand() {
		return new SingleOperandOrderable();
	}

	@Override
	protected IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>> nextForExprOperand() {
		return new ExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>() {

			@Override
			protected ISingleOperandOrderable nextForExprOperand0() {
				return OrderingItem.this.nextForSingleOperand();
			}

		};
	}

	@Override
	public ISingleOperandOrderable yield(final String yieldAlias) {
		return copy(new SingleOperandOrderable(), getTokens().yield(yieldAlias));
	}
}