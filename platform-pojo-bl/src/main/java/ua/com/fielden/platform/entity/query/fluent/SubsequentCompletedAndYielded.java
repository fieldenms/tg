package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentYieldedItemAlias;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

final class SubsequentCompletedAndYielded<ET extends AbstractEntity<?>> //
		extends CompletedCommon<ET> //
		implements ISubsequentCompletedAndYielded<ET> {
    
    protected SubsequentCompletedAndYielded(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
		return new EntityResultQueryModel<T>(getTokens().getValues(), resultType, getTokens().isYieldAll());
	}

	@Override
	public IFunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield() {
		return createFunctionYieldedLastArgument(getTokens().yield());
	}

	@Override
	public AggregatedResultQueryModel modelAsAggregate() {
		return new AggregatedResultQueryModel(getTokens().getValues(), getTokens().isYieldAll());
	}
	
	private FunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> createFunctionYieldedLastArgument(final Tokens tokens) {
		return new FunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET>(tokens) {

			@Override
			protected ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>> nextForFunctionYieldedLastArgument(final Tokens tokens) {
				return new SubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>, ET>(tokens);
			}

		};
	}
}