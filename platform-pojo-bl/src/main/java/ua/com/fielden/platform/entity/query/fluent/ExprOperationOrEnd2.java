package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

abstract class ExprOperationOrEnd2<T, ET extends AbstractEntity<?>> //
        extends ExprOperationOrEnd<IExprOperand2<T, ET>, IExprOperationOrEnd1<T, ET>, ET> //
        implements IExprOperationOrEnd2<T, ET> {

    protected ExprOperationOrEnd2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForExprOperationOrEnd2(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperationOrEnd1<T, ET> nextForExprOperationOrEnd(final EqlSentenceBuilder builder) {
        return new ExprOperationOrEnd1<T, ET>(builder) {

            @Override
            protected T nextForExprOperationOrEnd1(final EqlSentenceBuilder builder) {
                return ExprOperationOrEnd2.this.nextForExprOperationOrEnd2(builder);
            }

        };
    }

    @Override
    protected IExprOperand2<T, ET> nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
        return new ExprOperand2<T, ET>(builder) {

            @Override
            protected T nextForExprOperand2(final EqlSentenceBuilder builder) {
                return ExprOperationOrEnd2.this.nextForExprOperationOrEnd2(builder);
            }

        };
    }

}
