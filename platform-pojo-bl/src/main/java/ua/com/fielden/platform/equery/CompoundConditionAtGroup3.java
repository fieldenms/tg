package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup2;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup3;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup3;

final class CompoundConditionAtGroup3 extends AbstractLogicalCondition<IWhereAtGroup3> implements ICompoundConditionAtGroup3 {

    private final AbstractCloseGroup<ICompoundConditionAtGroup2> closeGroupImpl;

    CompoundConditionAtGroup3(final QueryTokens queryTokens) {
	super(queryTokens);
	this.closeGroupImpl = new AbstractCloseGroup<ICompoundConditionAtGroup2>(queryTokens) {
	    @Override
	    ICompoundConditionAtGroup2 createCloseGroup(final QueryTokens queryTokens) {
		return new CompoundConditionAtGroup2(queryTokens);
	    }
	};
    }

    @Override
    IWhereAtGroup3 createImplicitCondition(final QueryTokens queryTokens) {
	return new WhereAtGroup3(queryTokens);
    }

    @Override
    public ICompoundConditionAtGroup2 end() {
	return closeGroupImpl.end();
    }
}