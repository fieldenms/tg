package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup2;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup2;

final class CompoundConditionAtGroup2 extends AbstractLogicalCondition<IWhereAtGroup2> implements ICompoundConditionAtGroup2 {

    private final AbstractCloseGroup<ICompoundConditionAtGroup1> closeGroupImpl;

    CompoundConditionAtGroup2(final QueryTokens queryTokens) {
	super(queryTokens);
	this.closeGroupImpl = new AbstractCloseGroup<ICompoundConditionAtGroup1>(queryTokens) {
	    @Override
	    ICompoundConditionAtGroup1 createCloseGroup(final QueryTokens queryTokens) {
		return new CompoundConditionAtGroup1(queryTokens);
	    }
	};
    }

    @Override
    WhereAtGroup2 createImplicitCondition(final QueryTokens queryTokens) {
	return new WhereAtGroup2(queryTokens);
    }

    @Override
    public ICompoundConditionAtGroup1 end() {
	return closeGroupImpl.end();
    }
}
