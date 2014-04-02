package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;

final class JoinCompoundCondition1<ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IJoinWhere1<ET>, IJoinCompoundCondition0<ET>> implements IJoinCompoundCondition1<ET> {

    JoinCompoundCondition1(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    IJoinWhere1<ET> getParent() {
        return new JoinWhere1<ET>(getTokens());
    }

    @Override
    IJoinCompoundCondition0<ET> getParent2() {
        return new JoinCompoundCondition0<ET>(getTokens());
    }
}