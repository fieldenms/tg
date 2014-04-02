package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

class OrderingItem extends AbstractExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> implements IOrderingItem {

    OrderingItem(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    ISingleOperandOrderable getParent() {
        return new SingleOperandOrderable(getTokens());
    }

    @Override
    IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>> getParent2() {
        return new ExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>(getTokens(), getParent());
    }

    @Override
    public ISingleOperandOrderable yield(final String yieldAlias) {
        return new SingleOperandOrderable(getTokens().yield(yieldAlias));
    }
}