package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;

abstract class YieldExprItem2<T, ET extends AbstractEntity<?>> //
        extends YieldExprOperand<IYieldExprOperationOrEnd2<T, ET>, IYieldExprItem3<T, ET>, ET> //
        implements IYieldExprItem2<T, ET> {

    protected YieldExprItem2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldExprItem2(final EqlSentenceBuilder builder);

    @Override
    protected IYieldExprItem3<T, ET> nextForYieldExprOperand(final EqlSentenceBuilder builder) {
        return new YieldExprItem3<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem3(final EqlSentenceBuilder builder) {
                return YieldExprItem2.this.nextForYieldExprItem2(builder);
            }

        };
    }

    @Override
    protected IYieldExprOperationOrEnd2<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new YieldExprOperationOrEnd2<T, ET>(builder) {

            @Override
            protected T nextForYieldExprOperationOrEnd2(final EqlSentenceBuilder builder) {
                return YieldExprItem2.this.nextForYieldExprItem2(builder);
            }

        };
    }

}
