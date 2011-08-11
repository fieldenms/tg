package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;


class JoinOn extends AbstractQueryLink implements IJoinCondition {

    JoinOn(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IJoinWhere0 on() {
	return new JoinWhere0(getTokens().on());
    }
}
