package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinCompoundCondition3<ET extends AbstractEntity<?>> //
		extends CompoundCondition<IJoinWhere3<ET>, IJoinCompoundCondition2<ET>> //
		implements IJoinCompoundCondition3<ET> {

    public JoinCompoundCondition3(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IJoinWhere3<ET> nextForLogicalCondition(final Tokens tokens) {
		return new JoinWhere3<ET>(tokens);
	}

	@Override
	protected IJoinCompoundCondition2<ET> nextForCompoundCondition(final Tokens tokens) {
		return new JoinCompoundCondition2<ET>(tokens);
	}
}