package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

abstract class FunctionCompoundCondition2<T, ET extends AbstractEntity<?>> //
        extends CompoundCondition<IFunctionWhere2<T, ET>, IFunctionCompoundCondition1<T, ET>> //
        implements IFunctionCompoundCondition2<T, ET> {

    protected FunctionCompoundCondition2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionCompoundCondition2(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionWhere2<T, ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
        return new FunctionWhere2<T, ET>(builder) {

            @Override
            protected T nextForFunctionWhere2(final EqlSentenceBuilder builder) {
                return FunctionCompoundCondition2.this.nextForFunctionCompoundCondition2(builder);
            }

        };
    }

    @Override
    protected IFunctionCompoundCondition1<T, ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition1<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition1(final EqlSentenceBuilder builder) {
                return FunctionCompoundCondition2.this.nextForFunctionCompoundCondition2(builder);
            }

        };
    }

}
