package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

abstract class ExprOperand2<T, ET extends AbstractEntity<?>> //
        extends ExprOperand<IExprOperationOrEnd2<T, ET>, IExprOperand3<T, ET>, ET> //
        implements IExprOperand2<T, ET> {

    public ExprOperand2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForExprOperand2(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperand3<T, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand3<T, ET>(builder) {

            @Override
            protected T nextForExprOperand3(final EqlSentenceBuilder builder) {
                return ExprOperand2.this.nextForExprOperand2(builder);
            }

        };
    }

    @Override
    protected IExprOperationOrEnd2<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new ExprOperationOrEnd2<T, ET>(builder) {

            @Override
            protected T nextForExprOperationOrEnd2(final EqlSentenceBuilder builder) {
                return ExprOperand2.this.nextForExprOperand2(builder);
            }

        };
    }

}
