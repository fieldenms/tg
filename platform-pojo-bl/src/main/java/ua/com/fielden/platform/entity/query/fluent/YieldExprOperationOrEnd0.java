package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
        extends ExprOperationOrEnd<IYieldExprItem0<T, ET>, T, ET> //
        implements IYieldExprOperationOrEnd0<T, ET> {

    protected YieldExprOperationOrEnd0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForYieldExprOperationOrEnd0(final EqlSentenceBuilder builder);

    @Override
    protected T nextForExprOperationOrEnd(final EqlSentenceBuilder builder) {
        return nextForYieldExprOperationOrEnd0(builder);
    }

    @Override
    protected IYieldExprItem0<T, ET> nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
        return new YieldExprItem0<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem0(final EqlSentenceBuilder builder) {
                return YieldExprOperationOrEnd0.this.nextForYieldExprOperationOrEnd0(builder);
            }

        };
    }

    @Override
    public T endExpr() {
        return nextForExprOperationOrEnd(builder.endYieldExpression());
    }

}
