package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem1;

class Completed<ET extends AbstractEntity<?>> //
        extends CompletedAndYielded<ET> //
        implements ICompleted<ET> {

    public Completed(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public IFunctionLastArgument<ICompleted<ET>, ET> groupBy() {
        return createFunctionLastArgument(builder.groupBy());
    }

    @Override
    public IOrderingItem1<ET> orderBy() {
        return new OrderingItem1<>(builder.orderBy());
    }

    private FunctionLastArgument<ICompleted<ET>, ET> createFunctionLastArgument(final EqlSentenceBuilder builder) {
        return new FunctionLastArgument<ICompleted<ET>, ET>(builder) {

            @Override
            protected ICompleted<ET> nextForFunctionLastArgument(final EqlSentenceBuilder builder) {
                return new Completed<ET>(builder);
            }

        };
    }

}
