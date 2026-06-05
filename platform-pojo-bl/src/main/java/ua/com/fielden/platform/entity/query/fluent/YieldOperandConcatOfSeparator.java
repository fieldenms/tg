package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfSeparator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfSeparatorOperand;

abstract class YieldOperandConcatOfSeparator<T, ET extends AbstractEntity<?>>
        extends AbstractQueryLink
        implements IYieldOperandConcatOfSeparator<T, ET>
{

    protected YieldOperandConcatOfSeparator(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldOperandConcatOfSeparator(EqlSentenceBuilder builder);

    @Override
    public IYieldOperandConcatOfSeparatorOperand<T, ET> separator() {
        return new YieldOperandConcatOfSeparatorOperand<>(builder.separator()) {
            @Override
            protected T nextForYieldOperandConcatOfSeparator(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfSeparator.this.nextForYieldOperandConcatOfSeparator(builder);
            }
        };
    }

}
