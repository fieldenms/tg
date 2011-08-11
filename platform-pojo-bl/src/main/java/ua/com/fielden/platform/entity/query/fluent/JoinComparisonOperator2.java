package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;


class JoinComparisonOperator2 extends AbstractComparisonOperator<IJoinCompoundCondition2> implements IJoinComparisonOperator2 {

    JoinComparisonOperator2(final Tokens queryTokens) {
	super(queryTokens);
	}

    @Override
    IJoinCompoundCondition2 getParent1() {
	return new JoinCompoundCondition2(getTokens());
    }
}
