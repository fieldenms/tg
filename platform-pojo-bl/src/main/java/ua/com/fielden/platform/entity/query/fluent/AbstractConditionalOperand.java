package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExistenceOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.model.QueryModel;

abstract class AbstractConditionalOperand<T1 extends IComparisonOperator<T2>, T2 extends ILogicalOperator<?>> extends AbstractExpConditionalOperand<T1> implements IComparisonOperand<T1>, IExistenceOperator<T2> {
    abstract T2 getParent2();

    protected AbstractConditionalOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T2 exists(final QueryModel subQuery) {
	final T2 result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().exists(false, subQuery));
	return result;
    }

    @Override
    public T2 notExists(final QueryModel subQuery) {
	final T2 result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().exists(true, subQuery));
	return result;
    }

    @Override
    public T2 existsAnyOf(final QueryModel ... subQueries) {
	final T2 result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().existsAnyOf(false, subQueries));
	return result;
    }

    @Override
    public T2 notExistsAnyOf(final QueryModel ... subQueries) {
	final T2 result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().existsAnyOf(true, subQueries));
	return result;
    }

    @Override
    public T2 existsAllOf(final QueryModel ... subQueries) {
	final T2 result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().existsAllOf(false, subQueries));
	return result;
    }

    @Override
    public T2 notExistsAllOf(final QueryModel ... subQueries) {
	final T2 result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().existsAllOf(true, subQueries));
	return result;
    }

//    @Override
//    public IExpArgument0<T1> beginExp() {
//	return new ExpArgument0<T1>(getTokens().openExpression(), getParent());
//    }
}
