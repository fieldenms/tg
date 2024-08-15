package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

class StandaloneOrderBy_OrderingItem //
        extends ExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> //
        implements IOrderingItem {

    public StandaloneOrderBy_OrderingItem(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected ISingleOperandOrderable nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new StandaloneOrderBy_SingleOperandOrderable(builder);
    }

    @Override
    protected IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>(builder) {

            @Override
            protected ISingleOperandOrderable nextForExprOperand0(final EqlSentenceBuilder builder) {
                return StandaloneOrderBy_OrderingItem.this.nextForSingleOperand(builder);
            }

        };
    }

    @Override
    public ISingleOperandOrderable yield(final String yieldAlias) {
        return new StandaloneOrderBy_SingleOperandOrderable(builder.yield(yieldAlias));
    }

    @Override
    public IOrderingItemCloseable order(OrderingModel model) {
        return new StandaloneOrderBy_OrderingItemCloseable(builder.order(model));
    }

}
