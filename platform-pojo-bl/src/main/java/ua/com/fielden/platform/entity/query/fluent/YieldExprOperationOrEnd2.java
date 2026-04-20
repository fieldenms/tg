package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;

abstract class YieldExprOperationOrEnd2<T, ET extends AbstractEntity<?>> //
        extends ExprOperationOrEnd<IYieldExprItem2<T, ET>, IYieldExprOperationOrEnd1<T, ET>, ET> //
        implements IYieldExprOperationOrEnd2<T, ET> {

    protected YieldExprOperationOrEnd2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldExprOperationOrEnd2(final EqlSentenceBuilder builder);

    @Override
    protected IYieldExprOperationOrEnd1<T, ET> nextForExprOperationOrEnd(final EqlSentenceBuilder builder) {
        return new YieldExprOperationOrEnd1<T, ET>(builder) {

            @Override
            protected T nextForYieldExprOperationOrEnd1(final EqlSentenceBuilder builder) {
                return YieldExprOperationOrEnd2.this.nextForYieldExprOperationOrEnd2(builder);
            }

        };
    }

    @Override
    protected IYieldExprItem2<T, ET> nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
        return new YieldExprItem2<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem2(final EqlSentenceBuilder builder) {
                return YieldExprOperationOrEnd2.this.nextForYieldExprOperationOrEnd2(builder);
            }

        };
    }

}
