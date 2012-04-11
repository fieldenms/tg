package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;

public class CaseWhenFunctionEnd<T> extends AbstractQueryLink implements ICaseWhenFunctionEnd<T> {

    T parent;

    CaseWhenFunctionEnd(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public T end() {
	((AbstractQueryLink) parent).setTokens(getTokens());
	return parent;
    }
}
