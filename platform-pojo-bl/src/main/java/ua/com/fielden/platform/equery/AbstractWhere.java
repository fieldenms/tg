package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractSearchCondition;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractWhere;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

abstract class AbstractWhere<T1 extends IAbstractSearchCondition, T2> extends AbstractLeftSideSubject<T1> implements IAbstractWhere<T1, T2> {

    protected AbstractWhere(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    abstract T2 createLogicalCondition(final QueryTokens queryTokens);

    @Override
    public <E extends AbstractEntity> T2 exists(final IQueryModel<E> subQuery) {
	return createLogicalCondition(this.getTokens().exists(false, subQuery));
    }

    @Override
    public <E extends AbstractEntity> T2 notExists(final IQueryModel<E> subQuery) {
	return createLogicalCondition(this.getTokens().exists(true, subQuery));
    }

//    @Override
//    public T1 the(final String propertyName) {
//	return createSearchCondition(this.getTokens().the(propertyName));
//    }
}
