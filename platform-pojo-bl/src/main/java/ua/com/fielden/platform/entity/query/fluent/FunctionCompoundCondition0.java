package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

final class FunctionCompoundCondition0<T> extends AbstractQueryLink implements IFunctionCompoundCondition0<T> {
    T parent;

    FunctionCompoundCondition0(final Tokens queryTokens,  final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public ICaseWhenFunctionArgument<T> then() {
	return new CaseWhenFunctionArgument<T>(getTokens(), parent);
    }

    private AbstractLogicalCondition<IFunctionWhere0<T>> getLogicalCondition() {
	return new AbstractLogicalCondition<IFunctionWhere0<T>>(getTokens()) {

	    @Override
	    IFunctionWhere0<T> getParent() {
		return new FunctionWhere0<T>(getTokens(), parent);
	    }
	};
    }

    @Override
    public IFunctionWhere0<T> and() {
	return getLogicalCondition().and();
    }

    @Override
    public IFunctionWhere0<T> or() {
	return getLogicalCondition().or();
    }
}
