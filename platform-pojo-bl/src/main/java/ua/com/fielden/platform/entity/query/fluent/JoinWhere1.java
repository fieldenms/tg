package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;

final class JoinWhere1 extends AbstractWhere<IJoinComparisonOperator1, IJoinCompoundCondition1, IJoinWhere2> implements IJoinWhere1 {

    JoinWhere1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IJoinWhere2 getParent3() {
	return new JoinWhere2(getTokens());
    }

    @Override
    IJoinCompoundCondition1 getParent2() {
	return new JoinCompoundCondition1(getTokens());
    }

    @Override
    IJoinComparisonOperator1 getParent() {
	return new JoinComparisonOperator1(getTokens());
    }
}
