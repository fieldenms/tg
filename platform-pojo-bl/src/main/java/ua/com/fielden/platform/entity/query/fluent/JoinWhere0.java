package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;

final class JoinWhere0<ET extends AbstractEntity<?>> extends AbstractWhere<IJoinComparisonOperator0<ET>, IJoinCompoundCondition0<ET>, IJoinWhere1<ET>, ET> implements IJoinWhere0<ET> {

    JoinWhere0(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    protected IJoinWhere1<ET> getParent3() {
	return new JoinWhere1<ET>(getTokens());
    }

    @Override
    IJoinCompoundCondition0<ET> getParent2() {
	return new JoinCompoundCondition0<ET>(getTokens());
    }

    @Override
    IJoinComparisonOperator0<ET> getParent() {
	return new JoinComparisonOperator0<ET>(getTokens());
    }
}