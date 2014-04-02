package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

public class DateDiffFunctionBetween<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IDateDiffFunctionBetween<T, ET> {

    T parent;

    DateDiffFunctionBetween(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    public IFunctionLastArgument<T, ET> and() {
        return new FunctionLastArgument<T, ET>(getTokens(), parent);
    }
}