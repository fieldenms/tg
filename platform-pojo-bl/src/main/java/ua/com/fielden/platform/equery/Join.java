package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IMain.IJoin;
import ua.com.fielden.platform.equery.interfaces.IMain.IJoinCondition;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

class Join extends PlainJoin implements IJoin {

    Join(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition join(final Class<T> entityType, final String alias) {
	return new JoinOn(this.getTokens().join(entityType, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition leftJoin(final Class<T> entityType, final String alias) {
	return new JoinOn(this.getTokens().leftJoin(entityType, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition join(final IQueryModel model, final String alias) {
	return new JoinOn(this.getTokens().join(model, alias));
    }

    @Override
    public <T extends AbstractEntity> IJoinCondition leftJoin(final IQueryModel model, final String alias) {
	return new JoinOn(this.getTokens().leftJoin(model, alias));
    }
}
