package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;


class FromAlias extends Join implements IFromAlias {

    FromAlias(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IJoin as(final String alias) {
	return new Join(getTokens().joinAlias(alias));
    }
}
