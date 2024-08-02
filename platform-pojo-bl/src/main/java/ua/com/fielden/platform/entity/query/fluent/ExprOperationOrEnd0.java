package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
        extends ExprOperationOrEnd<IExprOperand0<T, ET>, T, ET> //
        implements IExprOperationOrEnd0<T, ET> {

    protected ExprOperationOrEnd0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForExprOperationOrEnd0(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperand0<T, ET> nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
        return new ExprOperand0<T, ET>(builder) {

            @Override
            protected T nextForExprOperand0(final EqlSentenceBuilder builder) {
                return ExprOperationOrEnd0.this.nextForExprOperationOrEnd0(builder);
            }

        };
    }

    @Override
    protected T nextForExprOperationOrEnd(final EqlSentenceBuilder builder) {
        return nextForExprOperationOrEnd0(builder);
    }

}
