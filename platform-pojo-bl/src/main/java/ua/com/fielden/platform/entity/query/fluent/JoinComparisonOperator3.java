package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;

final class JoinComparisonOperator3<ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IJoinCompoundCondition3<ET>, ET> //
		implements IJoinComparisonOperator3<ET> {

    public JoinComparisonOperator3(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IJoinCompoundCondition3<ET> nextForComparisonOperator(final Tokens tokens) {
		return new JoinCompoundCondition3<ET>(tokens);
	}
}
