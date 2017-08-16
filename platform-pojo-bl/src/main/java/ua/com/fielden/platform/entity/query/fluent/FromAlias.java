package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;

final class FromAlias<ET extends AbstractEntity<?>> //
		extends Join<ET> //
		implements IFromAlias<ET> {

	@Override
	public IJoin<ET> as(final String alias) {
		return copy(new Join<ET>(), getTokens().joinAlias(alias));
	}
}