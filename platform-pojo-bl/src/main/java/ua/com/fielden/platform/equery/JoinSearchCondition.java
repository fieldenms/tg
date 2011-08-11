package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.IExpRightArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinCompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinSearchCondition;

class JoinSearchCondition extends AbstractSearchCondition<IJoinCompoundCondition, IExpRightArgument<IJoinCompoundCondition>> implements IJoinSearchCondition {

    JoinSearchCondition(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinCompoundCondition createCompoundCondition(final QueryTokens queryTokens) {
	return new JoinCompoundCondition(queryTokens);
    }

    @Override
    IExpRightArgument<IJoinCompoundCondition> createConditionSubject(final QueryTokens queryTokens) {
	return new AbstractExpRightArgument<IJoinCompoundCondition>(queryTokens){
	    @Override
	    IJoinCompoundCondition createSearchCondition(final QueryTokens queryTokens) {
		return new JoinCompoundCondition(queryTokens);
	    }};
    }
}
