package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IFunctionLastArgument;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;

class Completed extends CompletedAndYielded implements ICompleted {

    Completed(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public ICompleted groupByProp(final String property) {
	return new Completed(this.getTokens().groupByProp(property));
    }

    @Override
    public ICompleted groupByExp(final String expression, final Object...values) {
	return new Completed(this.getTokens().groupByExp(expression, values));
    }

    @Override
    public <E extends AbstractEntity> ICompleted resultType(final Class<E> resultType) {
	return new Completed(this.getTokens().resultType(resultType));
    }


    @Override
    public IFunctionLastArgument<ICompleted> groupBy() {
	// TODO Auto-generated method stub
	return null;
    }
}
