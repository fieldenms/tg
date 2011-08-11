package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IMain.IJoinCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinWhere;

class JoinOn extends AbstractQueryLink implements IJoinCondition {

    JoinOn(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IJoinWhere on() {
	return new JoinWhere(this.getTokens());
    }
}
