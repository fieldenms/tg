package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

class OrderingItem1<ET extends AbstractEntity<?>>
        extends ExprOperand<ISingleOperandOrderable<ET>, IExprOperand0<ISingleOperandOrderable<ET>, ET>, ET>
        implements IOrderingItem1<ET>
{

    public OrderingItem1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected ISingleOperandOrderable<ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new SingleOperandOrderable<>(builder);
    }

    @Override
    protected IExprOperand0<ISingleOperandOrderable<ET>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand0<>(builder) {
            @Override
            protected ISingleOperandOrderable<ET> nextForExprOperand0(final EqlSentenceBuilder builder) {
                return OrderingItem1.this.nextForSingleOperand(builder);
            }
        };
    }

    @Override
    public ISingleOperandOrderable<ET> yield(final CharSequence yieldAlias) {
        return new SingleOperandOrderable<>(builder.yield(yieldAlias));
    }

    @Override
    public IOrderingItem<ET> order(final OrderingModel model) {
        return new OrderingItem<>(builder.order(model));
    }

}
