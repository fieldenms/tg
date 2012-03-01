package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentYieldedItemAlias;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class SubsequentCompletedAndYielded extends CompletedCommon implements ISubsequentCompletedAndYielded {

    SubsequentCompletedAndYielded(final Tokens queryTokens) {
	super(queryTokens);
    }

//    @Override
//    public <T extends AbstractEntity> UnorderedQueryModel model() {
//	return new UnorderedQueryModel(getTokens());
//    }

    @Override
    public <T extends AbstractEntity> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
	return new EntityResultQueryModel<T>(getTokens().getTokens(), resultType);
    }

    @Override
    public IFunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded>> yield() {
	return new FunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded>>(getTokens().yield(), new SubsequentYieldedItemAlias<ISubsequentCompletedAndYielded>(getTokens(), this));
    }

    @Override
    public AggregatedResultQueryModel modelAsAggregate() {
	return new AggregatedResultQueryModel(getTokens().getTokens());
    }
}
