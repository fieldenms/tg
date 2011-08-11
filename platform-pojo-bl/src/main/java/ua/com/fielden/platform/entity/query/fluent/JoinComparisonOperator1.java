package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator1;


class JoinComparisonOperator1 extends AbstractComparisonOperator<IJoinCompoundCondition1> implements IJoinComparisonOperator1 {

    JoinComparisonOperator1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinCompoundCondition1 getParent1() {
	return new JoinCompoundCondition1(getTokens());
    }
}
