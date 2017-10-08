package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;

final class JoinComparisonOperator1<ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IJoinCompoundCondition1<ET>, ET> //
		implements IJoinComparisonOperator1<ET> {

    public JoinComparisonOperator1(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IJoinCompoundCondition1<ET> nextForComparisonOperator(final Tokens tokens) {
		return new JoinCompoundCondition1<ET>(tokens);
	}
}