package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperand0<T, ET extends AbstractEntity<?>> //
        extends ExprOperand<IExprOperationOrEnd0<T, ET>, IExprOperand1<T, ET>, ET> //
        implements IExprOperand0<T, ET> {

    protected ExprOperand0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForExprOperand0(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperationOrEnd0<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new ExprOperationOrEnd0<T, ET>(builder) {

            @Override
            protected T nextForExprOperationOrEnd0(final EqlSentenceBuilder builder) {
                return ExprOperand0.this.nextForExprOperand0(builder);
            }

        };
    }

    @Override
    protected IExprOperand1<T, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand1<T, ET>(builder) {

            @Override
            protected T nextForExprOperand1(final EqlSentenceBuilder builder) {
                return ExprOperand0.this.nextForExprOperand0(builder);
            }

        };
    }

}
