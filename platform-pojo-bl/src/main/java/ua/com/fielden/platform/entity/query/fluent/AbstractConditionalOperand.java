package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
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
    public <E extends AbstractEntity> T2 exists(final QueryModel subQuery) {
	getTokens().exists(false, subQuery);
	return getParent2();
    }

    @Override
    public <E extends AbstractEntity> T2 notExists(final QueryModel subQuery) {
	getTokens().exists(true, subQuery);
	return getParent2();
    }

    @Override
    public <E extends AbstractEntity> T2 existsAnyOf(final QueryModel ... subQueries) {
	getTokens().existsAnyOf(false, subQueries);
	return getParent2();
    }

    @Override
    public <E extends AbstractEntity> T2 notExistsAnyOf(final QueryModel ... subQueries) {
	getTokens().existsAnyOf(true, subQueries);
	return getParent2();
    }

    @Override
    public <E extends AbstractEntity> T2 existsAllOf(final QueryModel ... subQueries) {
	getTokens().existsAllOf(false, subQueries);
	return getParent2();
    }

    @Override
    public <E extends AbstractEntity> T2 notExistsAllOf(final QueryModel ... subQueries) {
	getTokens().existsAllOf(true, subQueries);
	return getParent2();
    }

//    @Override
//    public IExpArgument0<T1> beginExp() {
//	return new ExpArgument0<T1>(getTokens().openExpression(), getParent());
//    }
}
