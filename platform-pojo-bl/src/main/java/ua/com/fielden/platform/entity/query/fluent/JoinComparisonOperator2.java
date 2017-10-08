package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;

final class JoinComparisonOperator2<ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IJoinCompoundCondition2<ET>, ET> //
		implements IJoinComparisonOperator2<ET> {

    public JoinComparisonOperator2(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IJoinCompoundCondition2<ET> nextForComparisonOperator(final Tokens tokens) {
		return new JoinCompoundCondition2<ET>(tokens);
	}
}