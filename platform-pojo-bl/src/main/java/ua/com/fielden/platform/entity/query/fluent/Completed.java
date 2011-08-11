package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

class Completed extends CompletedAndYielded implements ICompleted {

    Completed(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IFunctionLastArgument<ICompleted> groupBy() {
	return new FunctionLastArgument<ICompleted>(getTokens().groupBy(), this);
    }
}
