package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;

public class DateDiffFunction<T> extends AbstractQueryLink implements IDateDiffFunction<T> {

    T parent;

    DateDiffFunction(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public IDateDiffFunctionArgument<T> between() {
	return new DateDiffFunctionArgument<T>(getTokens(), parent);
    }
}
