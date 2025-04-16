package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprItem0<T, ET extends AbstractEntity<?>> //
        extends YieldExprOperand<IYieldExprOperationOrEnd0<T, ET>, IYieldExprItem1<T, ET>, ET> //
        implements IYieldExprItem0<T, ET> {

    protected YieldExprItem0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldExprItem0(final EqlSentenceBuilder builder);

    @Override
    protected IYieldExprItem1<T, ET> nextForYieldExprOperand(final EqlSentenceBuilder builder) {
        return new YieldExprItem1<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem1(final EqlSentenceBuilder builder) {
                return YieldExprItem0.this.nextForYieldExprItem0(builder);
            }

        };
    }

    @Override
    protected IYieldExprOperationOrEnd0<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new YieldExprOperationOrEnd0<T, ET>(builder) {
            @Override
            protected T nextForYieldExprOperationOrEnd0(final EqlSentenceBuilder builder) {
                return YieldExprItem0.this.nextForYieldExprItem0(builder);
            }

        };
    }

}
