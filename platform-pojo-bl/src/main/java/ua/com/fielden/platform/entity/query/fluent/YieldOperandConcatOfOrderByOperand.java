package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperandOrder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperandOrSeparator;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

abstract class YieldOperandConcatOfOrderByOperand<T, ET extends AbstractEntity<?>>
        extends SingleOperand<IYieldOperandConcatOfOrderByOperandOrder<T, ET>, ET>
        implements IYieldOperandConcatOfOrderByOperand<T, ET>
{

    protected YieldOperandConcatOfOrderByOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldOperandConcatOfOrderByOperand(EqlSentenceBuilder builder);

    @Override
    protected IYieldOperandConcatOfOrderByOperandOrder<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new YieldOperandConcatOfOrderByOperandOrder<>(builder) {
            @Override
            protected T nextForYieldOperandConcatOfOrderByOperandOrder(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfOrderByOperand.this.nextForYieldOperandConcatOfOrderByOperand(builder);
            }
        };
    }

    @Override
    public IYieldOperandConcatOfOrderByOperandOrSeparator<T, ET> order(final OrderingModel model) {
        return new YieldOperandConcatOfOrderByOperandOrSeparator<>(builder.order(model)) {
            @Override
            protected T nextForYieldOperandConcatOfOrderByOperandOrSeparator(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfOrderByOperand.this.nextForYieldOperandConcatOfOrderByOperand(builder);
            }
        };
    }

}
