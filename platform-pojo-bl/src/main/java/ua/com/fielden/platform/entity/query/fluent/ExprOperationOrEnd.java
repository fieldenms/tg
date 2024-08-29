package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd;

abstract class ExprOperationOrEnd<T1, T2, ET extends AbstractEntity<?>> //
        extends ArithmeticalOperator<T1> //
        implements IExprOperationOrEnd<T1, T2, ET> {

    protected ExprOperationOrEnd(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T2 nextForExprOperationOrEnd(final EqlSentenceBuilder builder);

    @Override
    public T2 endExpr() {
        return nextForExprOperationOrEnd(builder.endExpression());
    }

}
