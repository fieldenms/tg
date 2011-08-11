package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup3;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpRightArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchConditionAtGroup3;

class SearchConditionAtGroup3 extends AbstractSearchCondition<ICompoundConditionAtGroup3, IExpRightArgument<ICompoundConditionAtGroup3>> implements ISearchConditionAtGroup3 {

    SearchConditionAtGroup3(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundConditionAtGroup3 createCompoundCondition(final QueryTokens queryTokens) {
	return new CompoundConditionAtGroup3(queryTokens);
    }

    @Override
    IExpRightArgument<ICompoundConditionAtGroup3> createConditionSubject(final QueryTokens queryTokens) {
	return new AbstractExpRightArgument<ICompoundConditionAtGroup3>(queryTokens) {
	    @Override
	    ICompoundConditionAtGroup3 createSearchCondition(final QueryTokens queryTokens) {
		return new CompoundConditionAtGroup3(queryTokens);
	    }
	};
    }
}
