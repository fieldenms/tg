package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;

final class JoinWhere0 extends AbstractWhere<IJoinComparisonOperator0, IJoinCompoundCondition0, IJoinWhere1> implements IJoinWhere0 {

    JoinWhere0(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IJoinWhere1 getParent3() {
	return new JoinWhere1(getTokens());
    }

    @Override
    IJoinCompoundCondition0 getParent2() {
	return new JoinCompoundCondition0(getTokens());
    }

    @Override
    IJoinComparisonOperator0 getParent() {
	return new JoinComparisonOperator0(getTokens());
    }
}
