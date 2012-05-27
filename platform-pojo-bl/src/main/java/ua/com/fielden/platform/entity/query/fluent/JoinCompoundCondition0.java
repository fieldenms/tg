package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;


final class JoinCompoundCondition0<ET extends AbstractEntity<?>> extends Join<ET> implements IJoinCompoundCondition0<ET> {

    JoinCompoundCondition0(final Tokens queryTokens) {
	super(queryTokens);
    }

    private AbstractLogicalCondition<IJoinWhere0<ET>> getLogicalCondition() {
	return new AbstractLogicalCondition<IJoinWhere0<ET>>(getTokens()) {

	    @Override
	    IJoinWhere0<ET> getParent() {
		return new JoinWhere0<ET>(getTokens());
	    }
	};
    }

    @Override
    public IJoinWhere0<ET> and() {
	return getLogicalCondition().and();
    }

    @Override
    public IJoinWhere0<ET> or() {
	return getLogicalCondition().or();
    }
}