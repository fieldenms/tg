package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinCompoundCondition3 extends AbstractCompoundCondition<IJoinWhere3, IJoinCompoundCondition2> implements IJoinCompoundCondition3 {

    JoinCompoundCondition3(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinWhere3 getParent() {
	return new JoinWhere3(getTokens());
    }

    @Override
    IJoinCompoundCondition2 getParent2() {
	return new JoinCompoundCondition2(getTokens());
    }
}