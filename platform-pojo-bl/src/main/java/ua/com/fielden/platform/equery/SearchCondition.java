package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpRightArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchCondition;

class SearchCondition extends AbstractSearchCondition<ICompoundCondition, IExpRightArgument<ICompoundCondition>> implements ISearchCondition {

    SearchCondition(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition createCompoundCondition(final QueryTokens queryTokens) {
	return new CompoundCondition(queryTokens);
    }

    @Override
    IExpRightArgument<ICompoundCondition> createConditionSubject(final QueryTokens queryTokens) {
	return new AbstractExpRightArgument<ICompoundCondition>(queryTokens){
	    @Override
	    ICompoundCondition createSearchCondition(final QueryTokens queryTokens) {
		return new CompoundCondition(queryTokens);
	    }};
    }
}
