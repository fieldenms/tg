package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;

class JoinComparisonOperator0<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IJoinCompoundCondition0<ET>, ET> implements IJoinComparisonOperator0<ET> {

	@Override
    IJoinCompoundCondition0<ET> getParent1() {
        return new JoinCompoundCondition0<ET>();
    }
}