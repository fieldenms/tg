package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;


final class JoinCompoundCondition0 extends Join implements IJoinCompoundCondition0 {

    JoinCompoundCondition0(final Tokens queryTokens) {
	super(queryTokens);
    }

    private AbstractLogicalCondition<IJoinWhere0> getLogicalCondition() {
	return new AbstractLogicalCondition<IJoinWhere0>(getTokens()) {

	    @Override
	    IJoinWhere0 getParent() {
		return new JoinWhere0(getTokens());
	    }
	};
    }

    @Override
    public IJoinWhere0 and() {
	return getLogicalCondition().and();
    }

    @Override
    public IJoinWhere0 or() {
	return getLogicalCondition().or();
    }
}
