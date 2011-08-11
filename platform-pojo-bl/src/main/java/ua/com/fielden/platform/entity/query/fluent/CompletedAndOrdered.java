package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedAndOrdered;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrder;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class CompletedAndOrdered extends AbstractQueryLink implements ICompletedAndOrdered {

    CompletedAndOrdered(final Tokens queryTokens) {
	super(queryTokens);
    }

//    @Override
//    public <T extends AbstractEntity> ua.com.fielden.platform.entity.query.model.QueryModel model() {
//	return new QueryModel(getTokens());
//    }

    @Override
    public <T extends AbstractEntity> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
	return new EntityResultQueryModel<T>(getTokens().getTokens(), resultType);
    }

    @Override
    public IFunctionLastArgument<IOrder<ICompletedAndOrdered>> orderBy() {
	return new FunctionLastArgument<IOrder<ICompletedAndOrdered>>(getTokens().orderBy(), new Order<ICompletedAndOrdered>(getTokens(), this));
    }

    @Override
    public AggregatedResultQueryModel modelAsAggregate() {
	return new AggregatedResultQueryModel(getTokens().getTokens());
    }
}