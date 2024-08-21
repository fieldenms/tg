package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;

abstract class YieldExprOperationOrEnd1<T, ET extends AbstractEntity<?>> //
        extends ExprOperationOrEnd<IYieldExprItem1<T, ET>, IYieldExprOperationOrEnd0<T, ET>, ET> //
        implements IYieldExprOperationOrEnd1<T, ET> {

    protected YieldExprOperationOrEnd1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldExprOperationOrEnd1(final EqlSentenceBuilder builder);

    @Override
    protected IYieldExprOperationOrEnd0<T, ET> nextForExprOperationOrEnd(final EqlSentenceBuilder builder) {
        return new YieldExprOperationOrEnd0<T, ET>(builder) {

            @Override
            protected T nextForYieldExprOperationOrEnd0(final EqlSentenceBuilder builder) {
                return YieldExprOperationOrEnd1.this.nextForYieldExprOperationOrEnd1(builder);
            }

        };
    }

    @Override
    protected IYieldExprItem1<T, ET> nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
        return new YieldExprItem1<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem1(final EqlSentenceBuilder builder) {
                return YieldExprOperationOrEnd1.this.nextForYieldExprOperationOrEnd1(builder);
            }

        };
    }

    @Override
    public IYieldExprOperationOrEnd0<T, ET> endExpr() {
        return nextForExprOperationOrEnd(builder.endYieldExpression());
    }

}
