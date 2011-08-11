package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinWhere2 extends AbstractWhere<IJoinComparisonOperator2, IJoinCompoundCondition2, IJoinWhere3> implements IJoinWhere2 {

    JoinWhere2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IJoinWhere3 getParent3() {
	return new JoinWhere3(getTokens());
    }

    @Override
    IJoinCompoundCondition2 getParent2() {
	return new JoinCompoundCondition2(getTokens());
    }

    @Override
    IJoinComparisonOperator2 getParent() {
	return new JoinComparisonOperator2(getTokens());
    }
}
