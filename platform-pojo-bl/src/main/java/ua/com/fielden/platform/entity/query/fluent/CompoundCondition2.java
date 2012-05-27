package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;

final class CompoundCondition2<ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IWhere2<ET>, ICompoundCondition1<ET>> implements ICompoundCondition2<ET> {

    CompoundCondition2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IWhere2<ET> getParent() {
	return new Where2<ET>(getTokens());
    }

    @Override
    ICompoundCondition1<ET> getParent2() {
	return new CompoundCondition1<ET>(getTokens());
    }
}
