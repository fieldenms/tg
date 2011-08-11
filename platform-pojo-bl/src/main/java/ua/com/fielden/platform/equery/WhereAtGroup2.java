package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IDateDiffFunction;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup2;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchConditionAtGroup2;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup2;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup3;

final class WhereAtGroup2 extends AbstractWhere<ISearchConditionAtGroup2, ICompoundConditionAtGroup2> implements IWhereAtGroup2 {

    private final AbstractOpenGroup<IWhereAtGroup3> openGroupImpl;

    WhereAtGroup2(final QueryTokens queryTokens) {
	super(queryTokens);
	this.openGroupImpl = new AbstractOpenGroup<IWhereAtGroup3>(queryTokens) {
	    @Override
	    IWhereAtGroup3 createOpenGroup(final QueryTokens queryTokens) {
		return new WhereAtGroup3(queryTokens);
	    }
	};
    }

    @Override
    ISearchConditionAtGroup2 createSearchCondition(final QueryTokens queryTokens) {
	return new SearchConditionAtGroup2(queryTokens);
    }

    @Override
    ICompoundConditionAtGroup2 createLogicalCondition(final QueryTokens queryTokens) {
	return new CompoundConditionAtGroup2(queryTokens);
    }

    @Override
    public IWhereAtGroup3 begin() {
	return openGroupImpl.begin();
    }

    @Override
    public IWhereAtGroup3 notBegin() {
	return openGroupImpl.notBegin();
    }

    @Override
    public IExpArgument<ISearchConditionAtGroup2> beginExp() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IDateDiffFunction<ISearchConditionAtGroup2> countDays() {
	// TODO Auto-generated method stub
	return null;
    }
}