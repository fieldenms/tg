package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprOperationOrEnd3<T, ET extends AbstractEntity<?>> //
        extends ExprOperationOrEnd<IYieldExprItem3<T, ET>, IYieldExprOperationOrEnd2<T, ET>, ET> //
        implements IYieldExprOperationOrEnd3<T, ET> {

    protected YieldExprOperationOrEnd3(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldExprOperationOrEnd3(final EqlSentenceBuilder builder);

    @Override
    protected IYieldExprOperationOrEnd2<T, ET> nextForExprOperationOrEnd(final EqlSentenceBuilder builder) {
        return new YieldExprOperationOrEnd2<T, ET>(builder) {

            @Override
            protected T nextForYieldExprOperationOrEnd2(final EqlSentenceBuilder builder) {
                return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3(builder);
            }

        };
    }

    @Override
    protected IYieldExprItem3<T, ET> nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
        return new YieldExprItem3<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem3(final EqlSentenceBuilder builder) {
                return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3(builder);
            }

        };
    }

}
