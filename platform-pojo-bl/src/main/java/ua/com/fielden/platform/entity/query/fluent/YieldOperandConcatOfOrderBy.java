package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderBy;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfOrderByOperand;

abstract class YieldOperandConcatOfOrderBy<T, ET extends AbstractEntity<?>>
        extends AbstractQueryLink
        implements IYieldOperandConcatOfOrderBy<T, ET>
{

    protected YieldOperandConcatOfOrderBy(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldOperandConcatOfOrderBy(EqlSentenceBuilder builder);

    @Override
    public IYieldOperandConcatOfOrderByOperand<T, ET> orderBy() {
        return new YieldOperandConcatOfOrderByOperand<>(builder.orderBy()) {
            @Override
            protected T nextForYieldOperandConcatOfOrderByOperand(final EqlSentenceBuilder builder) {
                return YieldOperandConcatOfOrderBy.this.nextForYieldOperandConcatOfOrderBy(builder);
            }
        };
    }

}
