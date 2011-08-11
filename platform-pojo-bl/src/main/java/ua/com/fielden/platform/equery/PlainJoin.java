package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IMain.IPlainJoin;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;

class PlainJoin extends Completed implements IPlainJoin {

    PlainJoin(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IWhere where() {
	return new Where(this.getTokens().where());
    }
}
