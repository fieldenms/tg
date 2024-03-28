package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

abstract class RoundFunctionArgument<T, ET extends AbstractEntity<?>> //
        extends ExprOperand<IRoundFunctionTo<T>, IExprOperand0<IRoundFunctionTo<T>, ET>, ET> //
        implements IRoundFunctionArgument<T, ET> {

    protected RoundFunctionArgument(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForRoundFunctionArgument(final EqlSentenceBuilder builder);

    @Override
    protected IExprOperand0<IRoundFunctionTo<T>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
        return new ExprOperand0<IRoundFunctionTo<T>, ET>(builder) {

            @Override
            protected IRoundFunctionTo<T> nextForExprOperand0(final EqlSentenceBuilder builder) {
                return new RoundFunctionTo<T>(builder) {

                    @Override
                    protected T nextForRoundFunctionTo(final EqlSentenceBuilder builder) {
                        return RoundFunctionArgument.this.nextForRoundFunctionArgument(builder);
                    }

                };
            }

        };
    }

    @Override
    protected IRoundFunctionTo<T> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new RoundFunctionTo<T>(builder) {

            @Override
            protected T nextForRoundFunctionTo(final EqlSentenceBuilder builder) {
                return RoundFunctionArgument.this.nextForRoundFunctionArgument(builder);
            }

        };
    }

}
