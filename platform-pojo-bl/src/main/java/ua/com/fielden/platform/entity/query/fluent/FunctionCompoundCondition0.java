package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

final class FunctionCompoundCondition0<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IFunctionCompoundCondition0<T, ET> {
    T parent;

    FunctionCompoundCondition0(final Tokens queryTokens,  final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public ICaseWhenFunctionArgument<T, ET> then() {
	return new CaseWhenFunctionArgument<T, ET>(getTokens(), parent);
    }

    private AbstractLogicalCondition<IFunctionWhere0<T, ET>> getLogicalCondition() {
	return new AbstractLogicalCondition<IFunctionWhere0<T, ET>>(getTokens()) {

	    @Override
	    IFunctionWhere0<T, ET> getParent() {
		return new FunctionWhere0<T, ET>(getTokens(), parent);
	    }
	};
    }

    @Override
    public IFunctionWhere0<T, ET> and() {
	return getLogicalCondition().and();
    }

    @Override
    public IFunctionWhere0<T, ET> or() {
	return getLogicalCondition().or();
    }
}
