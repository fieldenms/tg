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

	@Override
	public EntityResultQueryModel<ET> model() {
		return new EntityResultQueryModel<ET>(getTokens().getValues(), (Class<ET>) getTokens().getMainSourceType(),	false);
	}

	@Override
	public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
		return new EntityResultQueryModel<T>(getTokens().getValues(), resultType, getTokens().isYieldAll());
	}

	@Override
	public IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield() {
		return copy(createFunctionYieldedLastArgument(), getTokens().yield());
	}

	private FunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> createFunctionYieldedLastArgument() {
		return new FunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET>() {

			@Override
			protected IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>> nextForFunctionYieldedLastArgument() {
				return new FirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>() {
					@Override
					protected ISubsequentCompletedAndYielded<ET> nextForFirstYieldedItemAlias() {
						return new SubsequentCompletedAndYielded<ET>();
					}
				};
			}

		};
	}

	@Override
	public ISubsequentCompletedAndYielded<ET> yieldAll() {
		return copy(new SubsequentCompletedAndYielded<ET>(), getTokens().yieldAll());
	}
}