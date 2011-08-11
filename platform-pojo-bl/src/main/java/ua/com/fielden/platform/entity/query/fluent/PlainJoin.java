package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IPlainJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;


class PlainJoin extends Completed implements IPlainJoin {

    PlainJoin(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IWhere0 where() {
	return new Where0(getTokens().where());
    }
}
