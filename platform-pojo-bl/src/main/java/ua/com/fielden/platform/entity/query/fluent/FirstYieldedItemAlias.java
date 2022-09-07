package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

abstract class FirstYieldedItemAlias<T> //
		extends AbstractQueryLink //
		implements IFirstYieldedItemAlias<T> {

    protected FirstYieldedItemAlias(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFirstYieldedItemAlias(final Tokens tokens);

	@Override
	public T as(final String alias) {
		return nextForFirstYieldedItemAlias(getTokens().as(alias));
	}

    @Override
    public T as(final IConvertableToPath alias) {
        return as(alias.toPath());
    }

	@Override
	public T asRequired(final String alias) {
		return nextForFirstYieldedItemAlias(getTokens().asRequired(alias));
	}

    @Override
    public T asRequired(final IConvertableToPath alias) {
        return asRequired(alias.toPath());
    }

	@Override
	public <ET extends AbstractEntity<?>> EntityResultQueryModel<ET> modelAsEntity(final Class<ET> entityType) {
		return new EntityResultQueryModel<ET>(getTokens().getValues(), entityType, getTokens().isYieldAll());
	}

	@Override
	public PrimitiveResultQueryModel modelAsPrimitive() {
		return new PrimitiveResultQueryModel(getTokens().getValues());
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