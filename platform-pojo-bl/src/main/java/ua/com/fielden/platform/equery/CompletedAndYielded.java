package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IExpressionAlias;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndYielded;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

class CompletedAndYielded extends CompletedAndOrdered implements ICompletedAndYielded {

    CompletedAndYielded(final QueryTokens queryTokens) {
	super(queryTokens);
    }


//    @Override
//    public ICompleted groupByProp(final String property) {
//	return new CompletedAndYielded(this.getTokens().groupByProp(property));
//    }
//
//    @Override
//    public ICompleted groupByExp(final String expression, final Object...values) {
//	return new CompletedAndYielded(this.getTokens().groupByExp(expression, values));
//    }

    @Override
    public <E extends AbstractEntity> ICompletedAndYielded yieldModel(final IQueryModel<? extends E> subModel, final String alias) {
	return new CompletedAndYielded(this.getTokens().yieldModel(subModel, alias));
    }

    @Override
    public ICompletedAndYielded yieldExp(final String propertyExpression, final String alias, final Object...values) {
	return new CompletedAndYielded(this.getTokens().yieldExp(propertyExpression, alias, values));
    }

    @Override
    public ICompletedAndYielded yieldProp(final String propertyExpression, final String alias) {
	return new CompletedAndYielded(this.getTokens().yieldProp(propertyExpression, alias));
    }

    @Override
    public ICompletedAndYielded yieldProp(final String propertyExpression) {
	return new CompletedAndYielded(this.getTokens().yieldProp(propertyExpression));
    }

    @Override
    public ICompletedAndYielded yieldValue(final Object value, final String alias) {
	return new CompletedAndYielded(this.getTokens().yieldValue(value, alias));
    }

    @Override
    public <T extends AbstractEntity> IQueryModel<T> model() {
	return new QueryModel<T>(getTokens());
    }

    @Override
    public <T extends AbstractEntity> IQueryModel<T> model(final Class<T> resultType) {
	return new QueryModel<T>(getTokens(), resultType);
    }

//    @Override
//    public <E extends AbstractEntity> ICompleted resultType(final Class<E> resultType) {
//	return new CompletedAndYielded(this.getTokens().resultType(resultType));
//    }


//    @Override
//    public IFunctionLastArgument<ICompleted> groupBy() {
//	// TODO Auto-generated method stub
//	return null;
//    }


    @Override
    public IFunctionYieldedLastArgument<IExpressionAlias<ICompletedAndYielded>> yield() {
	// TODO Auto-generated method stub
	return null;
    }
}
