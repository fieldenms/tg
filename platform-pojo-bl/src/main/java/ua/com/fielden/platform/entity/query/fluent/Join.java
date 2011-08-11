package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCondition;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class Join extends PlainJoin implements IJoin {

    Join(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition join(final Class<T> entityType, final String alias) {
	return new JoinOn(getTokens().innerJoin(entityType, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition leftJoin(final Class<T> entityType, final String alias) {
	return new JoinOn(getTokens().leftJoin(entityType, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition join(final AggregatedResultQueryModel model, final String alias) {
	return new JoinOn(getTokens().innerJoin(model, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition join(final EntityResultQueryModel<T> model, final String alias) {
	return new JoinOn(getTokens().innerJoin(model, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition leftJoin(final AggregatedResultQueryModel model, final String alias) {
	return new JoinOn(getTokens().leftJoin(model, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition leftJoin(final EntityResultQueryModel<T> model, final String alias) {
	return new JoinOn(getTokens().leftJoin(model, alias));
    }

}
