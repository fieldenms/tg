package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;

abstract class FunctionComparisonOperator3<T, ET extends AbstractEntity<?>> //
        extends ComparisonOperator<IFunctionCompoundCondition3<T, ET>, ET> //
        implements IFunctionComparisonOperator3<T, ET> {

    protected FunctionComparisonOperator3(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionComparisonOperator3(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionCompoundCondition3<T, ET> nextForComparisonOperator(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition3<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition3(final EqlSentenceBuilder builder) {
                return FunctionComparisonOperator3.this.nextForFunctionComparisonOperator3(builder);
            }

        };
    }

}
