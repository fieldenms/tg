package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;

final class JoinCompoundCondition1 extends AbstractCompoundCondition<IJoinWhere1, IJoinCompoundCondition0> implements IJoinCompoundCondition1 {

    JoinCompoundCondition1(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IJoinWhere1 getParent() {
	return new JoinWhere1(getTokens());
    }

    @Override
    IJoinCompoundCondition0 get() {
	return new JoinCompoundCondition0(getTokens());
    }
}
