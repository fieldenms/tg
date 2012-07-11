package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

public class CaseWhenFunctionWhen<T, ET extends AbstractEntity<?>> extends CaseWhenFunctionElseEnd<T, ET> implements ICaseWhenFunctionWhen<T, ET> {

    T parent;

    CaseWhenFunctionWhen(final Tokens queryTokens, final T parent) {
	super(queryTokens, parent);
    }

    @Override
    public IFunctionWhere0<T, ET> when() {
	return new FunctionWhere0<T, ET>(getTokens(), parent);
    }
}