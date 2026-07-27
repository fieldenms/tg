package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperandOrSeparator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfSeparatorOperand;

abstract class YieldOperandConcatOfOrderByOperandOrSeparator<T, ET extends AbstractEntity<?>>
        extends YieldOperandConcatOfOrderByOperand<T, ET>
        implements IYieldOperandConcatOfOrderByOperandOrSeparator<T, ET>
{

    protected YieldOperandConcatOfOrderByOperandOrSeparator(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldOperandConcatOfOrderByOperandOrSeparator(EqlSentenceBuilder builder);

    @Override
    protected T nextForYieldOperandConcatOfOrderByOperand(final EqlSentenceBuilder builder) {
        return nextForYieldOperandConcatOfOrderByOperandOrSeparator(builder);
    }

    @Override
    public IYieldOperandConcatOfSeparatorOperand<T, ET> separator() {
        return new YieldOperandConcatOfSeparator<T, ET>(builder) {
            @Override
            protected T nextForYieldOperandConcatOfSeparator(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfOrderByOperandOrSeparator.this.nextForYieldOperandConcatOfOrderByOperandOrSeparator(builder);
            }
        }.separator();
    }

}
