package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;

abstract class YieldExprItem1<T, ET extends AbstractEntity<?>> //
        extends YieldExprOperand<IYieldExprOperationOrEnd1<T, ET>, IYieldExprItem2<T, ET>, ET> //
        implements IYieldExprItem1<T, ET> {

    protected YieldExprItem1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldExprItem1(final EqlSentenceBuilder builder);

    @Override
    protected IYieldExprItem2<T, ET> nextForYieldExprOperand(final EqlSentenceBuilder builder) {
        return new YieldExprItem2<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem2(final EqlSentenceBuilder builder) {
                return YieldExprItem1.this.nextForYieldExprItem1(builder);
            }

        };
    }

    @Override
    protected IYieldExprOperationOrEnd1<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new YieldExprOperationOrEnd1<T, ET>(builder) {

            @Override
            protected T nextForYieldExprOperationOrEnd1(final EqlSentenceBuilder builder) {
                return YieldExprItem1.this.nextForYieldExprItem1(builder);
            }

        };
    }

}
