package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

final class CompoundCondition1 extends AbstractCompoundCondition<IWhere1, ICompoundCondition0> implements ICompoundCondition1 {

    CompoundCondition1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IWhere1 getParent() {
	return new Where1(getTokens());
    }

    @Override
    ICompoundCondition0 getParent2() {
	return new CompoundCondition0(getTokens());
    }
}
