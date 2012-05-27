package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

class Completed<ET extends AbstractEntity<?>> extends CompletedAndYielded<ET> implements ICompleted<ET> {

    Completed(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IFunctionLastArgument<ICompleted<ET>, ET> groupBy() {
	return new FunctionLastArgument<ICompleted<ET>, ET>(getTokens().groupBy(), this);
    }
}