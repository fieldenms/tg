package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;

final class CompoundCondition2 extends AbstractCompoundCondition<IWhere2, ICompoundCondition1> implements ICompoundCondition2 {

    CompoundCondition2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IWhere2 getParent() {
	return new Where2(getTokens());
    }

    @Override
    ICompoundCondition1 getParent2() {
	return new CompoundCondition1(getTokens());
    }
}
