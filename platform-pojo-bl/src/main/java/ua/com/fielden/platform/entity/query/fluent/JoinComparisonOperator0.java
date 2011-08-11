package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator0;

class JoinComparisonOperator0 extends AbstractComparisonOperator<IJoinCompoundCondition0> implements IJoinComparisonOperator0 {

    JoinComparisonOperator0(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinCompoundCondition0 getParent1() {
	return new JoinCompoundCondition0(getTokens());
    }
}
