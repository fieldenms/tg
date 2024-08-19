package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class CompletedAndYielded<ET extends AbstractEntity<?>> //
        extends CompletedCommon<ET> //
        implements ICompletedAndYielded<ET> {

    protected CompletedAndYielded(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public EntityResultQueryModel<ET> model() {
        return new EntityResultQueryModel<ET>(builder.model().getTokenSource(), (Class<ET>) builder.getMainSourceType(), false);
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
        return new EntityResultQueryModel<T>(builder.modelAsEntity(resultType).getTokenSource(), resultType, builder.isYieldAll());
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> yieldAll() {
        return new SubsequentCompletedAndYielded<ET>(builder.yieldAll());
    }

    @Override
    public IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield() {
        return createFunctionYieldedLastArgument(builder.yield());
    }

    private FunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> createFunctionYieldedLastArgument(final EqlSentenceBuilder builder) {
        return new FunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET>(builder) {

            @Override
            protected IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>> nextForFunctionYieldedLastArgument(final EqlSentenceBuilder builder) {
                return new FirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>(builder) {
                    @Override
                    protected ISubsequentCompletedAndYielded<ET> nextForFirstYieldedItemAlias(final EqlSentenceBuilder builder) {
                        return new SubsequentCompletedAndYielded<ET>(builder);
                    }
                };
            }

        };
    }

}
