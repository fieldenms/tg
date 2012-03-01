package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

class OrderingItem extends AbstractSingleOperand<ISingleOperandOrderable> implements IOrderingItem {

    OrderingItem(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ISingleOperandOrderable getParent() {
	return new SingleOperandOrderable(getTokens());
    }
}
