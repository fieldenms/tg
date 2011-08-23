package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCondition;


class JoinAlias extends JoinOn implements IJoinAlias {

    JoinAlias(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IJoinCondition as(final String alias) {
	return new JoinOn(getTokens().joinAlias(alias));
    }
}
