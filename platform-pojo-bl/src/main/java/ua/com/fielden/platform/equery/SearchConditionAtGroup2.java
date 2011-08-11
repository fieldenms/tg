package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup2;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpRightArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchConditionAtGroup2;

class SearchConditionAtGroup2 extends AbstractSearchCondition<ICompoundConditionAtGroup2, IExpRightArgument<ICompoundConditionAtGroup2>> implements ISearchConditionAtGroup2 {

    SearchConditionAtGroup2(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundConditionAtGroup2 createCompoundCondition(final QueryTokens queryTokens) {
	return new CompoundConditionAtGroup2(queryTokens);
    }

    @Override
    IExpRightArgument<ICompoundConditionAtGroup2> createConditionSubject(final QueryTokens queryTokens) {
	return new AbstractExpRightArgument<ICompoundConditionAtGroup2>(queryTokens){
	    @Override
	    ICompoundConditionAtGroup2 createSearchCondition(final QueryTokens queryTokens) {
		return new CompoundConditionAtGroup2(queryTokens);
	    }};
    }
}
