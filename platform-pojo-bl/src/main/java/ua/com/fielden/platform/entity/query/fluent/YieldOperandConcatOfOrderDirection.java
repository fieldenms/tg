package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderDirection;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfSeparator;

abstract class YieldOperandConcatOfOrderDirection<T, ET extends AbstractEntity<?>>
        extends AbstractQueryLink
        implements IYieldOperandConcatOfOrderDirection<T, ET>
{

    protected YieldOperandConcatOfOrderDirection(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract IYieldOperandConcatOfSeparator<T, ET> nextForConcatOfOrderDirection(EqlSentenceBuilder builder);

    @Override
    public IYieldOperandConcatOfSeparator<T, ET> asc() {
        return nextForConcatOfOrderDirection(builder.asc());
    }

    @Override
    public IYieldOperandConcatOfSeparator<T, ET> desc() {
        return nextForConcatOfOrderDirection(builder.desc());
    }

}
