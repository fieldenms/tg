package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator3;


class JoinComparisonOperator3 extends AbstractComparisonOperator<IJoinCompoundCondition3> implements IJoinComparisonOperator3 {

    JoinComparisonOperator3(final Tokens queryTokens) {
	super(queryTokens);
	}

    @Override
    IJoinCompoundCondition3 getParent1() {
	return new JoinCompoundCondition3(getTokens());
    }
}
