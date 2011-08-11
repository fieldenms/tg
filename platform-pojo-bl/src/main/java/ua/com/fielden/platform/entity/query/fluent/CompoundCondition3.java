package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class CompoundCondition3 extends AbstractCompoundCondition<IWhere3, ICompoundCondition2> implements ICompoundCondition3 {

    CompoundCondition3(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IWhere3 getParent() {
	return new Where3(getTokens());
    }

    @Override
    ICompoundCondition2 get() {
	return new CompoundCondition2(getTokens());
    }
}