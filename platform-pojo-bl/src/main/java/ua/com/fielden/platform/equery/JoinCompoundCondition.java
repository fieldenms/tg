package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinCompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinWhere;

final class JoinCompoundCondition extends Join implements IJoinCompoundCondition {

    private final AbstractLogicalCondition<IJoinWhere> logicalConditionImpl;

    JoinCompoundCondition(final QueryTokens queryTokens) {
	super(queryTokens);
	this.logicalConditionImpl = new AbstractLogicalCondition<IJoinWhere>(queryTokens) {
	    @Override
	    IJoinWhere createImplicitCondition(final QueryTokens queryTokens) {
		return new JoinWhere(queryTokens);
	    }
	};
    }

    @Override
    public IJoinWhere and() {
	return logicalConditionImpl.and();
    }

    @Override
    public IJoinWhere or() {
	return logicalConditionImpl.or();
    }
}
