package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;

final class CompoundCondition extends Completed implements ICompoundCondition {

    private final AbstractLogicalCondition<IWhere> logicalConditionImpl;

    CompoundCondition(final QueryTokens queryTokens) {
	super(queryTokens);
	this.logicalConditionImpl = new AbstractLogicalCondition<IWhere>(queryTokens) {
	    @Override
	    IWhere createImplicitCondition(final QueryTokens queryTokens) {
		return new Where(queryTokens);
	    }
	};
    }

    @Override
    public IWhere and() {
	return logicalConditionImpl.and();
    }

    @Override
    public IWhere or() {
	return logicalConditionImpl.or();
    }
}
