package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

final class CompoundCondition1<ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IWhere1<ET>, ICompoundCondition0<ET>> implements ICompoundCondition1<ET> {

    CompoundCondition1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IWhere1<ET> getParent() {
	return new Where1<ET>(getTokens());
    }

    @Override
    ICompoundCondition0<ET> getParent2() {
	return new CompoundCondition0<ET>(getTokens());
    }
}