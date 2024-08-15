package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class FunctionLastArgument<T, ET extends AbstractEntity<?>> //
        extends ExprOperand<T, IExprOperand0<T, ET>, ET> //
        implements IFunctionLastArgument<T, ET> {

    protected FunctionLastArgument(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionLastArgument(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperand0<T, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand0<T, ET>(builder) {

            @Override
            protected T nextForExprOperand0(final EqlSentenceBuilder builder) {
                return FunctionLastArgument.this.nextForFunctionLastArgument(builder);
            }

        };
    }

    @Override
    protected T nextForSingleOperand(final EqlSentenceBuilder builder) {
        return nextForFunctionLastArgument(builder);
    }

}
