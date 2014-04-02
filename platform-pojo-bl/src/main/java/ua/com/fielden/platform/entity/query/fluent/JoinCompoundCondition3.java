package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinCompoundCondition3<ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IJoinWhere3<ET>, IJoinCompoundCondition2<ET>> implements IJoinCompoundCondition3<ET> {

    JoinCompoundCondition3(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    IJoinWhere3<ET> getParent() {
        return new JoinWhere3<ET>(getTokens());
    }

    @Override
    IJoinCompoundCondition2<ET> getParent2() {
        return new JoinCompoundCondition2<ET>(getTokens());
    }
}