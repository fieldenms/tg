package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperandOrSeparator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperandOrder;

abstract class YieldOperandConcatOfOrderByOperandOrder<T, ET extends AbstractEntity<?>>
        extends AbstractQueryLink
        implements IYieldOperandConcatOfOrderByOperandOrder<T, ET>
{

    protected YieldOperandConcatOfOrderByOperandOrder(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldOperandConcatOfOrderByOperandOrder(EqlSentenceBuilder builder);

    @Override
    public IYieldOperandConcatOfOrderByOperandOrSeparator<T, ET> asc() {
        return new YieldOperandConcatOfOrderByOperandOrSeparator<>(builder.asc()) {
            @Override
            protected T nextForYieldOperandConcatOfOrderByOperandOrSeparator(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfOrderByOperandOrder.this.nextForYieldOperandConcatOfOrderByOperandOrder(builder);
            }
        };
    }

    @Override
    public IYieldOperandConcatOfOrderByOperandOrSeparator<T, ET> desc() {
        return new YieldOperandConcatOfOrderByOperandOrSeparator<>(builder.desc()) {
            @Override
            protected T nextForYieldOperandConcatOfOrderByOperandOrSeparator(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfOrderByOperandOrder.this.nextForYieldOperandConcatOfOrderByOperandOrder(builder);
            }
        };
    }

}
