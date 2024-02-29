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

	protected SubsequentCompletedAndYielded(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
		return new EntityResultQueryModel<T>(builder.modelAsEntity(resultType).getTokens(), resultType, builder.isYieldAll());
	}

	@Override
	public IFunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield() {
		return createFunctionYieldedLastArgument(builder.yield());
	}

	@Override
	public AggregatedResultQueryModel modelAsAggregate() {
		return new AggregatedResultQueryModel(builder.modelAsAggregate().getTokens(), builder.isYieldAll());
	}

	private FunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> createFunctionYieldedLastArgument(final EqlSentenceBuilder builder) {
		return new FunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET>(builder) {

			@Override
			protected ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>> nextForFunctionYieldedLastArgument(final EqlSentenceBuilder builder) {
				return new SubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>, ET>(builder);
			}

		};
	}

}
