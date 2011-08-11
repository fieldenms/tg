package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IDateDiffFunction;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup3;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchConditionAtGroup3;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup3;

final class WhereAtGroup3 extends AbstractWhere<ISearchConditionAtGroup3, ICompoundConditionAtGroup3> implements IWhereAtGroup3 {

    WhereAtGroup3(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ISearchConditionAtGroup3 createSearchCondition(final QueryTokens queryTokens) {
	return new SearchConditionAtGroup3(queryTokens);
    }

    @Override
    ICompoundConditionAtGroup3 createLogicalCondition(final QueryTokens queryTokens) {
	return new CompoundConditionAtGroup3(queryTokens);
    }

    @Override
    public IExpArgument<ISearchConditionAtGroup3> beginExp() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IDateDiffFunction<ISearchConditionAtGroup3> countDays() {
	// TODO Auto-generated method stub
	return null;
    }
}
