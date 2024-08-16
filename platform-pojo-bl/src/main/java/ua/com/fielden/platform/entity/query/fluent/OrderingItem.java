package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

class OrderingItem //
        extends ExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> //
        implements IOrderingItem {

    public OrderingItem(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected ISingleOperandOrderable nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new SingleOperandOrderable(builder);
    }

    @Override
    protected IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>(builder) {

            @Override
            protected ISingleOperandOrderable nextForExprOperand0(final EqlSentenceBuilder builder) {
                return OrderingItem.this.nextForSingleOperand(builder);
            }

        };
    }

    @Override
    public ISingleOperandOrderable yield(final CharSequence yieldAlias) {
        return new SingleOperandOrderable(builder.yield(yieldAlias));
    }

    @Override
    public IOrderingItemCloseable order(OrderingModel model) {
        return new OrderingItemCloseable(builder.order(model));
    }

}
