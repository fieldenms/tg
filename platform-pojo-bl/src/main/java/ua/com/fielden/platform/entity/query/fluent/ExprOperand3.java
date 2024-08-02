package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperand3<T, ET extends AbstractEntity<?>> //
        extends SingleOperand<IExprOperationOrEnd3<T, ET>, ET> //
        implements IExprOperand3<T, ET> {

    protected ExprOperand3(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForExprOperand3(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperationOrEnd3<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new ExprOperationOrEnd3<T, ET>(builder) {

            @Override
            protected T nextForExprOperationOrEnd3(final EqlSentenceBuilder builder) {
                return ExprOperand3.this.nextForExprOperand3(builder);
            }

        };
    }

}
