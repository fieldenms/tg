package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

abstract class ExprOperand1<T, ET extends AbstractEntity<?>> //
        extends ExprOperand<IExprOperationOrEnd1<T, ET>, IExprOperand2<T, ET>, ET> //
        implements IExprOperand1<T, ET> {

    protected ExprOperand1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForExprOperand1(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperand2<T, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand2<T, ET>(builder) {

            @Override
            protected T nextForExprOperand2(final EqlSentenceBuilder builder) {
                return ExprOperand1.this.nextForExprOperand1(builder);
            }

        };
    }

    @Override
    protected IExprOperationOrEnd1<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new ExprOperationOrEnd1<T, ET>(builder) {

            @Override
            protected T nextForExprOperationOrEnd1(final EqlSentenceBuilder builder) {
                return ExprOperand1.this.nextForExprOperand1(builder);
            }

        };
    }

}
