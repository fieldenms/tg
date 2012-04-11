package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;

final class JoinCompoundCondition2 extends AbstractCompoundCondition<IJoinWhere2, IJoinCompoundCondition1> implements IJoinCompoundCondition2 {

    JoinCompoundCondition2(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinWhere2 getParent() {
	return new JoinWhere2(getTokens());
    }

    @Override
    IJoinCompoundCondition1 getParent2() {
	return new JoinCompoundCondition1(getTokens());
    }
}
