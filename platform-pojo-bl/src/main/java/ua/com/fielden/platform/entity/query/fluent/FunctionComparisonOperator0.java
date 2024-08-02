package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;

abstract class FunctionComparisonOperator0<T, ET extends AbstractEntity<?>> //
        extends ComparisonOperator<IFunctionCompoundCondition0<T, ET>, ET> //
        implements IFunctionComparisonOperator0<T, ET> {

    protected FunctionComparisonOperator0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionComparisonOperator0(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionCompoundCondition0<T, ET> nextForComparisonOperator(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition0<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition0(final EqlSentenceBuilder builder) {
                return FunctionComparisonOperator0.this.nextForFunctionComparisonOperator0(builder);
            }

        };
    }

}
