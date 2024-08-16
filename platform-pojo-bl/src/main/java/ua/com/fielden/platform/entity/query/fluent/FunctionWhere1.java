package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

abstract class FunctionWhere1<T, ET extends AbstractEntity<?>> //
        extends Where<IFunctionComparisonOperator1<T, ET>, IFunctionCompoundCondition1<T, ET>, IFunctionWhere2<T, ET>, ET> //
        implements IFunctionWhere1<T, ET> {

    protected FunctionWhere1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionWhere1(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionWhere2<T, ET> nextForWhere(final EqlSentenceBuilder builder) {
        return new FunctionWhere2<T, ET>(builder) {

            @Override
            protected T nextForFunctionWhere2(final EqlSentenceBuilder builder) {
                return FunctionWhere1.this.nextForFunctionWhere1(builder);
            }

        };
    }

    @Override
    protected IFunctionCompoundCondition1<T, ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition1<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition1(final EqlSentenceBuilder builder) {
                return FunctionWhere1.this.nextForFunctionWhere1(builder);
            }

        };
    }

    @Override
    protected IFunctionComparisonOperator1<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new FunctionComparisonOperator1<T, ET>(builder) {

            @Override
            protected T nextForFunctionComparisonOperator1(final EqlSentenceBuilder builder) {
                return FunctionWhere1.this.nextForFunctionWhere1(builder);
            }

        };
    }

}
