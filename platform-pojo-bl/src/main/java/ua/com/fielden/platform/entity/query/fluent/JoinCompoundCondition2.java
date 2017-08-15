package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;

final class JoinCompoundCondition2<ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IJoinWhere2<ET>, IJoinCompoundCondition1<ET>> implements IJoinCompoundCondition2<ET> {

    @Override
    IJoinWhere2<ET> nextForAbstractLogicalCondition() {
        return new JoinWhere2<ET>();
    }

    @Override
    IJoinCompoundCondition1<ET> nextForAbstractCompoundCondition() {
        return new JoinCompoundCondition1<ET>();
    }
}