package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfSeparatorOperand;

abstract class YieldOperandConcatOfSeparatorOperand<T, ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements IYieldOperandConcatOfSeparatorOperand<T, ET>
{

    protected YieldOperandConcatOfSeparatorOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldOperandConcatOfSeparator(EqlSentenceBuilder builder);

    @Override
    public T val(final CharSequence value) {
        return nextForYieldOperandConcatOfSeparator(builder.val(value.toString()));
    }

    @Override
    public T param(final CharSequence paramName) {
        return nextForYieldOperandConcatOfSeparator(builder.param(paramName));
    }

    @Override
    public T param(final Enum<?> paramName) {
        return param(paramName.toString());
    }

}
