package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;

class JoinComparisonOperator3<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IJoinCompoundCondition3<ET>, ET> implements IJoinComparisonOperator3<ET> {

    JoinComparisonOperator3(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    IJoinCompoundCondition3<ET> getParent1() {
        return new JoinCompoundCondition3<ET>(getTokens());
    }
}
