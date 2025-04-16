package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

abstract class FunctionWhere0<T, ET extends AbstractEntity<?>> //
        extends Where<IFunctionComparisonOperator0<T, ET>, IFunctionCompoundCondition0<T, ET>, IFunctionWhere1<T, ET>, ET> //
        implements IFunctionWhere0<T, ET> {

    protected FunctionWhere0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionWhere0(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionWhere1<T, ET> nextForWhere(final EqlSentenceBuilder builder) {
        return new FunctionWhere1<T, ET>(builder) {

            @Override
            protected T nextForFunctionWhere1(final EqlSentenceBuilder builder) {
                return FunctionWhere0.this.nextForFunctionWhere0(builder);
            }

        };
    }

    @Override
    protected IFunctionCompoundCondition0<T, ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition0<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition0(final EqlSentenceBuilder builder) {
                return FunctionWhere0.this.nextForFunctionWhere0(builder);
            }

        };
    }

    @Override
    protected IFunctionComparisonOperator0<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new FunctionComparisonOperator0<T, ET>(builder) {

            @Override
            protected T nextForFunctionComparisonOperator0(final EqlSentenceBuilder builder) {
                return FunctionWhere0.this.nextForFunctionWhere0(builder);
            }
        };
    }

}
