package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;


final class JoinWhere3<ET extends AbstractEntity<?>> extends AbstractConditionalOperand<IJoinComparisonOperator3<ET>, IJoinCompoundCondition3<ET>, ET> implements IJoinWhere3<ET> {

    JoinWhere3(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinCompoundCondition3<ET> getParent2() {
	return new JoinCompoundCondition3<ET>(getTokens());
    }

    @Override
    IJoinComparisonOperator3<ET> getParent() {
	return new JoinComparisonOperator3<ET>(getTokens());
    }
}
