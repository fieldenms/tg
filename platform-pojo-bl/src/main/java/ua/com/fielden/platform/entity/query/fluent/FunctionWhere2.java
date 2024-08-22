package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionWhere2<T, ET extends AbstractEntity<?>> //
        extends Where<IFunctionComparisonOperator2<T, ET>, IFunctionCompoundCondition2<T, ET>, IFunctionWhere3<T, ET>, ET> //
        implements IFunctionWhere2<T, ET> {

    protected FunctionWhere2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionWhere2(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionWhere3<T, ET> nextForWhere(final EqlSentenceBuilder builder) {
        return new FunctionWhere3<T, ET>(builder) {

            @Override
            protected T nextForFunctionWhere3(final EqlSentenceBuilder builder) {
                return FunctionWhere2.this.nextForFunctionWhere2(builder);
            }

        };
    }

    @Override
    protected IFunctionCompoundCondition2<T, ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition2<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition2(final EqlSentenceBuilder builder) {
                return FunctionWhere2.this.nextForFunctionWhere2(builder);
            }

        };
    }

    @Override
    protected IFunctionComparisonOperator2<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new FunctionComparisonOperator2<T, ET>(builder) {

            @Override
            protected T nextForFunctionComparisonOperator2(final EqlSentenceBuilder builder) {
                return FunctionWhere2.this.nextForFunctionWhere2(builder);
            }

        };
    }

}
