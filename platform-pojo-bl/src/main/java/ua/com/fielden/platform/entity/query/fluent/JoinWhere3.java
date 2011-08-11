package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;


final class JoinWhere3 extends AbstractConditionalOperand<IJoinComparisonOperator3, IJoinCompoundCondition3> implements IJoinWhere3 {

    JoinWhere3(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinCompoundCondition3 getParent2() {
	return new JoinCompoundCondition3(getTokens());
    }

    @Override
    IJoinComparisonOperator3 getParent() {
	return new JoinComparisonOperator3(getTokens());
    }
}
