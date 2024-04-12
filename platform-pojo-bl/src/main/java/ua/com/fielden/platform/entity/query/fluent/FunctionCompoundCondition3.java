package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionCompoundCondition3<T, ET extends AbstractEntity<?>> //
        extends CompoundCondition<IFunctionWhere3<T, ET>, IFunctionCompoundCondition2<T, ET>> //
        implements IFunctionCompoundCondition3<T, ET> {

    protected FunctionCompoundCondition3(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFunctionCompoundCondition3(final EqlSentenceBuilder builder);

    @Override
    protected IFunctionWhere3<T, ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
        return new FunctionWhere3<T, ET>(builder) {

            @Override
            protected T nextForFunctionWhere3(final EqlSentenceBuilder builder) {
                return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3(builder);
            }

        };
    }

    @Override
    protected IFunctionCompoundCondition2<T, ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
        return new FunctionCompoundCondition2<T, ET>(builder) {

            @Override
            protected T nextForFunctionCompoundCondition2(final EqlSentenceBuilder builder) {
                return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3(builder);
            }

        };
    }

}
