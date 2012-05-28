package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

public class FirstYieldedItemAlias<T> extends AbstractQueryLink implements IFirstYieldedItemAlias<T> {
    T parent;
    protected FirstYieldedItemAlias(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public T as(final String alias) {
	return copy(parent, getTokens().as(alias));
    }

    @Override
    public <ET extends AbstractEntity<?>> EntityResultQueryModel<ET> modelAsEntity(final Class<ET> entityType) {
	return new EntityResultQueryModel<ET>(getTokens().getValues(), entityType);
    }

    @Override
    public PrimitiveResultQueryModel modelAsPrimitive() {
	return new PrimitiveResultQueryModel(getTokens().getValues());
    }
}