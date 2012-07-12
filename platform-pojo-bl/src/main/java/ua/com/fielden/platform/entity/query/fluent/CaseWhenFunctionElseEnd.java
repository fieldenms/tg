package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionElseEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;

public class CaseWhenFunctionElseEnd<T, ET extends AbstractEntity<?>> extends CaseWhenFunctionEnd<T> implements ICaseWhenFunctionElseEnd<T, ET> {

    CaseWhenFunctionElseEnd(final Tokens queryTokens, final T parent) {
	super(queryTokens, parent);
    }

    @Override
    public ICaseWhenFunctionLastArgument<T, ET> otherwise() {
	return new CaseWhenFunctionLastArgument<T, ET>(getTokens(), parent);
    }
}