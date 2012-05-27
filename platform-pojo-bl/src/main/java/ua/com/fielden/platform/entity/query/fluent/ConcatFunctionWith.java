package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;

final class ConcatFunctionWith<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IConcatFunctionWith<T, ET> {
    T parent;

    ConcatFunctionWith(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public T end() {
	return copy(parent, getTokens().endOfFunction());
    }

    @Override
    public IConcatFunctionArgument<T, ET> with() {
	return new ConcatFunctionArgument<T, ET>(this.getTokens(), parent);
    }
}