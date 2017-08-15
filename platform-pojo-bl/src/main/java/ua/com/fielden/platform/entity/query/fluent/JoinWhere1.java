package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;

final class JoinWhere1<ET extends AbstractEntity<?>> extends AbstractWhere<IJoinComparisonOperator1<ET>, IJoinCompoundCondition1<ET>, IJoinWhere2<ET>, ET> implements IJoinWhere1<ET> {

    @Override
    protected IJoinWhere2<ET> nextForAbstractWhere() {
        return new JoinWhere2<ET>();
    }

    @Override
    IJoinCompoundCondition1<ET> nextForAbstractConditionalOperand() {
        return new JoinCompoundCondition1<ET>();
    }

    @Override
    IJoinComparisonOperator1<ET> nextForAbstractSingleOperand() {
        return new JoinComparisonOperator1<ET>();
    }
}