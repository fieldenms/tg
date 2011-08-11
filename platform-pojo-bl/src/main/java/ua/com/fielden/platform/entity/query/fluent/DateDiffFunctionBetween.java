package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

public class DateDiffFunctionBetween<T> extends AbstractQueryLink implements IDateDiffFunctionBetween<T> {

    T parent;

    DateDiffFunctionBetween(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public IFunctionLastArgument<T> and() {
	return new FunctionLastArgument<T>(getTokens(), parent);
    }

}
