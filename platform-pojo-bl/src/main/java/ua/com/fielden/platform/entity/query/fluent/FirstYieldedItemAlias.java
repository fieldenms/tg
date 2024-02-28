package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

abstract class FirstYieldedItemAlias<T> //
		extends AbstractQueryLink //
		implements IFirstYieldedItemAlias<T> {

	protected FirstYieldedItemAlias(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForFirstYieldedItemAlias(final EqlSentenceBuilder builder);

	@Override
	public T as(final String alias) {
		return nextForFirstYieldedItemAlias(builder.as(alias));
	}

	@Override
	public T as(final IConvertableToPath alias) {
		return as(alias.toPath());
	}

	@Override
	public T asRequired(final String alias) {
		return nextForFirstYieldedItemAlias(builder.asRequired(alias));
	}

	@Override
	public T asRequired(final IConvertableToPath alias) {
		return asRequired(alias.toPath());
	}

	@Override
	public <ET extends AbstractEntity<?>> EntityResultQueryModel<ET> modelAsEntity(final Class<ET> entityType) {
		return new EntityResultQueryModel<ET>(builder.getTokens(), entityType, builder.isYieldAll());
	}

	@Override
	public PrimitiveResultQueryModel modelAsPrimitive() {
		return new PrimitiveResultQueryModel(builder.getTokens());
	}

	@Override
	public T as(final Enum alias) {
		return as(alias.toString());
	}

	@Override
	public T asRequired(final Enum alias) {
		return asRequired(alias.toString());
	}

}
