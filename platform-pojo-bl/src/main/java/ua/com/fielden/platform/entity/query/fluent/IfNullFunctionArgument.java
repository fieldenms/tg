package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

abstract class IfNullFunctionArgument<T, ET extends AbstractEntity<?>> //
        extends ExprOperand<IIfNullFunctionThen<T, ET>, IExprOperand0<IIfNullFunctionThen<T, ET>, ET>, ET> //
        implements IIfNullFunctionArgument<T, ET> {

    protected IfNullFunctionArgument(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForIfNullFunctionArgument(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperand0<IIfNullFunctionThen<T, ET>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand0<IIfNullFunctionThen<T, ET>, ET>(builder) {

            @Override
            protected IIfNullFunctionThen<T, ET> nextForExprOperand0(final EqlSentenceBuilder builder) {
                return new IfNullFunctionThen<T, ET>(builder) {

                    @Override
                    protected T nextForIfNullFunctionThen(final EqlSentenceBuilder builder) {
                        return IfNullFunctionArgument.this.nextForIfNullFunctionArgument(builder);
                    }

                };
            }

        };
    }

    @Override
    protected IIfNullFunctionThen<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new IfNullFunctionThen<T, ET>(builder) {

            @Override
            protected T nextForIfNullFunctionThen(final EqlSentenceBuilder builder) {
                return IfNullFunctionArgument.this.nextForIfNullFunctionArgument(builder);
            }

        };
    }

}
