package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

abstract class ExprOperationOrEnd1<T, ET extends AbstractEntity<?>> //
        extends ExprOperationOrEnd<IExprOperand1<T, ET>, IExprOperationOrEnd0<T, ET>, ET> //
        implements IExprOperationOrEnd1<T, ET> {

    protected ExprOperationOrEnd1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForExprOperationOrEnd1(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperationOrEnd0<T, ET> nextForExprOperationOrEnd(final EqlSentenceBuilder builder) {
        return new ExprOperationOrEnd0<T, ET>(builder) {

            @Override
            protected T nextForExprOperationOrEnd0(final EqlSentenceBuilder builder) {
                return ExprOperationOrEnd1.this.nextForExprOperationOrEnd1(builder);
            }

        };
    }

    @Override
    protected IExprOperand1<T, ET> nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
        return new ExprOperand1<T, ET>(builder) {

            @Override
            protected T nextForExprOperand1(final EqlSentenceBuilder builder) {
                return ExprOperationOrEnd1.this.nextForExprOperationOrEnd1(builder);
            }
        };
    }

}
