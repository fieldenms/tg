package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;


final class CompoundCondition0<ET extends AbstractEntity<?>> extends Completed<ET> implements ICompoundCondition0<ET> {

    CompoundCondition0(final Tokens queryTokens) {
	super(queryTokens);
    }

    private AbstractLogicalCondition<IWhere0<ET>> getLogicalCondition() {
	return new AbstractLogicalCondition<IWhere0<ET>>(getTokens()) {

	    @Override
	    IWhere0<ET> getParent() {
		return new Where0<ET>(getTokens());
	    }
	};
    }

    @Override
    public IWhere0<ET> and() {
	return getLogicalCondition().and();
    }

    @Override
    public IWhere0<ET> or() {
	return getLogicalCondition().or();
    }
}
