package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpRightArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchConditionAtGroup1;

class SearchConditionAtGroup1 extends AbstractSearchCondition<ICompoundConditionAtGroup1, IExpRightArgument<ICompoundConditionAtGroup1>> implements ISearchConditionAtGroup1 {

    SearchConditionAtGroup1(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundConditionAtGroup1 createCompoundCondition(final QueryTokens queryTokens) {
	return new CompoundConditionAtGroup1(queryTokens);
    }

    @Override
    IExpRightArgument<ICompoundConditionAtGroup1> createConditionSubject(final QueryTokens queryTokens) {
	return new AbstractExpRightArgument<ICompoundConditionAtGroup1>(queryTokens){
	    @Override
	    ICompoundConditionAtGroup1 createSearchCondition(final QueryTokens queryTokens) {
		return new CompoundConditionAtGroup1(queryTokens);
	    }};
    }
}
