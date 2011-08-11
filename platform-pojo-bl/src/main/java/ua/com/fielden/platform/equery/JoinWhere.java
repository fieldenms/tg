package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinSearchCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinWhere;

final class JoinWhere extends AbstractWhere<IOthers.IJoinSearchCondition, IOthers.IJoinCompoundCondition> implements IJoinWhere {

    JoinWhere(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IOthers.IJoinCompoundCondition createLogicalCondition(final QueryTokens queryTokens) {
	return new JoinCompoundCondition(queryTokens);
    }

    @Override
    IOthers.IJoinSearchCondition createSearchCondition(final QueryTokens queryTokens) {
	return new JoinSearchCondition(queryTokens);
    }

    @Override
    public IExpArgument<IJoinSearchCondition> beginExp() {
	// TODO Auto-generated method stub
	return null;
    }
}
