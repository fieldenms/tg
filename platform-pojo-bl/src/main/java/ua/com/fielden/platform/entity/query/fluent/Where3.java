package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;


final class Where3 extends AbstractConditionalOperand<IComparisonOperator3, ICompoundCondition3> implements IWhere3 {

    Where3(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    ICompoundCondition3 getParent2() {
	return new CompoundCondition3(getTokens());
    }

    @Override
    IComparisonOperator3 getParent() {
	return new ComparisonOperator3(getTokens());
    }
}
