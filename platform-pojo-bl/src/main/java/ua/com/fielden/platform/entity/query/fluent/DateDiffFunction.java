package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;

public class DateDiffFunction<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IDateDiffFunction<T, ET> {

    T parent;

    DateDiffFunction(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public IDateDiffFunctionArgument<T, ET> between() {
	return new DateDiffFunctionArgument<T, ET>(getTokens(), parent);
    }
}