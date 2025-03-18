package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionWhere3<T, ET extends AbstractEntity<?>> //
        extends ConditionalOperand<IFunctionComparisonOperator3<T, ET>, IFunctionCompoundCondition3<T, ET>, ET> //
        implements IFunctionWhere3<T, ET> {

    protected FunctionWhere3(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionWhere3(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionCompoundCondition3<T, ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition3<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition3(final EqlSentenceBuilder builder) {
                return FunctionWhere3.this.nextForFunctionWhere3(builder);
            }

        };
    }

    @Override
    protected IFunctionComparisonOperator3<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new FunctionComparisonOperator3<T, ET>(builder) {

            @Override
            protected T nextForFunctionComparisonOperator3(final EqlSentenceBuilder builder) {
                return FunctionWhere3.this.nextForFunctionWhere3(builder);
            }

        };
    }

}
