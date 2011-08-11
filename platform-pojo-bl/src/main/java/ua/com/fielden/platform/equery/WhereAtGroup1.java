package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IDateDiffFunction;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchConditionAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup2;

final class WhereAtGroup1 extends AbstractWhere<ISearchConditionAtGroup1, ICompoundConditionAtGroup1> implements IWhereAtGroup1 {

    private final AbstractOpenGroup<IWhereAtGroup2> openGroupImpl;

    WhereAtGroup1(final QueryTokens queryTokens) {
	super(queryTokens);
	this.openGroupImpl = new AbstractOpenGroup<IWhereAtGroup2>(queryTokens) {
	    @Override
	    IWhereAtGroup2 createOpenGroup(final QueryTokens queryTokens) {
		return new WhereAtGroup2(queryTokens);
	    }
	};
    }

    @Override
    ISearchConditionAtGroup1 createSearchCondition(final QueryTokens queryTokens) {
	return new SearchConditionAtGroup1(queryTokens);
    }

    @Override
    ICompoundConditionAtGroup1 createLogicalCondition(final QueryTokens queryTokens) {
	return new CompoundConditionAtGroup1(queryTokens);
    }

    @Override
    public IWhereAtGroup2 begin() {
	return openGroupImpl.begin();
    }

    @Override
    public IWhereAtGroup2 notBegin() {
	return openGroupImpl.notBegin();
    }

    @Override
    public IExpArgument<ISearchConditionAtGroup1> beginExp() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IDateDiffFunction<ISearchConditionAtGroup1> countDays() {
	// TODO Auto-generated method stub
	return null;
    }
}
