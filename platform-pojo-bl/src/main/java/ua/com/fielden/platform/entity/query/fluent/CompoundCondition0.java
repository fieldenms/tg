package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;


final class CompoundCondition0 extends Completed implements ICompoundCondition0 {

    CompoundCondition0(final Tokens queryTokens) {
	super(queryTokens);
    }

    private AbstractLogicalCondition<IWhere0> get() {
	return new AbstractLogicalCondition<IWhere0>(getTokens()) {

	    @Override
	    IWhere0 getParent() {
		return new Where0(getTokens());
	    }
	};
    }

    @Override
    public IWhere0 and() {
	return get().and();
    }

    @Override
    public IWhere0 or() {
	return get().or();
    }
}
