package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup1;

final class CompoundConditionAtGroup1 extends AbstractLogicalCondition<IWhereAtGroup1> implements ICompoundConditionAtGroup1 {

    private final AbstractCloseGroup<ICompoundCondition> closeGroupImpl;

    CompoundConditionAtGroup1(final QueryTokens queryTokens) {
	super(queryTokens);
	this.closeGroupImpl = new AbstractCloseGroup<ICompoundCondition>(queryTokens) {
	    @Override
	    ICompoundCondition createCloseGroup(final QueryTokens queryTokens) {
		return new CompoundCondition(queryTokens);
	    }
	};
    }

    @Override
    IWhereAtGroup1 createImplicitCondition(final QueryTokens queryTokens) {
	return new WhereAtGroup1(queryTokens);
    }

    @Override
    public ICompoundCondition end() {
	return closeGroupImpl.end();
    }
}
