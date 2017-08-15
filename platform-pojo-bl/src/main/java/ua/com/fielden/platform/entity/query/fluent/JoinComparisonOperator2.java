package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;

class JoinComparisonOperator2<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IJoinCompoundCondition2<ET>, ET> implements IJoinComparisonOperator2<ET> {

    @Override
    IJoinCompoundCondition2<ET> getParent1() {
        return new JoinCompoundCondition2<ET>();
    }
}