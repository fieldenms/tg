package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinWhere2<ET extends AbstractEntity<?>> extends AbstractWhere<IJoinComparisonOperator2<ET>, IJoinCompoundCondition2<ET>, IJoinWhere3<ET>, ET> implements IJoinWhere2<ET> {

    JoinWhere2(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    protected IJoinWhere3<ET> getParent3() {
        return new JoinWhere3<ET>(getTokens());
    }

    @Override
    IJoinCompoundCondition2<ET> getParent2() {
        return new JoinCompoundCondition2<ET>(getTokens());
    }

    @Override
    IJoinComparisonOperator2<ET> getParent() {
        return new JoinComparisonOperator2<ET>(getTokens());
    }
}