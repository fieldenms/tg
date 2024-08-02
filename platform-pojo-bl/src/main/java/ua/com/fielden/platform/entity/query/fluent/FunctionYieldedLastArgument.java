package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

abstract class FunctionYieldedLastArgument<T, ET extends AbstractEntity<?>> //
        extends YieldExprOperand<T, IYieldExprItem0<T, ET>, ET> //
        implements IFunctionYieldedLastArgument<T, ET> {

    protected FunctionYieldedLastArgument(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionYieldedLastArgument(final EqlSentenceBuilder builder);

    @Override
    protected IYieldExprItem0<T, ET> nextForYieldExprOperand(final EqlSentenceBuilder builder) {
        return new YieldExprItem0<T, ET>(builder) {

            @Override
            protected T nextForYieldExprItem0(final EqlSentenceBuilder builder) {
                return FunctionYieldedLastArgument.this.nextForFunctionYieldedLastArgument(builder);
            }

        };
    }

    @Override
    protected T nextForSingleOperand(final EqlSentenceBuilder builder) {
        return nextForFunctionYieldedLastArgument(builder);
    }

}
