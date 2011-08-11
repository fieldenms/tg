package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractLeftSideSubject;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IDateDiffFunction;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IFunctionLastArgument;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IIfNullFunctionArgument;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IRoundFunctionArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.IFunctionWhere;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

abstract class AbstractLeftSideSubject<T> extends AbstractQueryLink implements IAbstractLeftSideSubject<T> {

    protected AbstractLeftSideSubject(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    abstract T createSearchCondition(final QueryTokens queryTokens);

    @Override
    public T exp(final String expression, final Object... values) {
	return createSearchCondition(this.getTokens().exp(expression, values));
    }

    @Override
    public T val(final Object value) {
	return createSearchCondition(this.getTokens().val(value));
    }

    @Override
    public <E extends AbstractEntity> T model(final IQueryModel<E> model) {
	return createSearchCondition(this.getTokens().model(model));
    }

    @Override
    public T param(final String paramName) {
	return createSearchCondition(this.getTokens().param(paramName));
    }

    @Override
    public T prop(final String propertyName) {
	return createSearchCondition(this.getTokens().prop(propertyName));
    }

    @Override
    public IDateDiffFunction<T> countDays() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public T now() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IIfNullFunctionArgument<T> ifNull() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<T> upperCase() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public IFunctionWhere<T> caseWhen() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IRoundFunctionArgument<T> round() {
	// TODO Auto-generated method stub
	return null;
    }


}
