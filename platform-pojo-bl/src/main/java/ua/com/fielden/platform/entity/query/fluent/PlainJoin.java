package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IPlainJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;

class PlainJoin<ET extends AbstractEntity<?>> //
		extends Completed<ET> //
		implements IPlainJoin<ET> {

    protected PlainJoin(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public IWhere0<ET> where() {
		return new Where0<ET>(getTokens().where());
	}
}