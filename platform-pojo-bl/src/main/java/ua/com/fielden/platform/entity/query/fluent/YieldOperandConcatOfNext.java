package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfNext;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfSeparatorOperand;

abstract class YieldOperandConcatOfNext<T, ET extends AbstractEntity<?>>
        extends AbstractQueryLink
        implements IYieldOperandConcatOfNext<T, ET>
{

    protected YieldOperandConcatOfNext(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldOperandConcatOfNext(EqlSentenceBuilder builder);

    @Override
    public IYieldOperandConcatOfOrderByOperand<T, ET> orderBy() {
        return new YieldOperandConcatOfOrderBy<T, ET>(builder) {
            @Override
            protected T nextForYieldOperandConcatOfOrderBy(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfNext.this.nextForYieldOperandConcatOfNext(builder);
            }
        }.orderBy();
    }

    @Override
    public IYieldOperandConcatOfSeparatorOperand<T, ET> separator() {
        return new YieldOperandConcatOfSeparator<T, ET>(builder) {
            @Override
            protected T nextForYieldOperandConcatOfSeparator(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfNext.this.nextForYieldOperandConcatOfNext(builder);
            }
        }.separator();
    }

}
