package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;

final class JoinComparisonOperator2<ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IJoinCompoundCondition2<ET>, ET> //
		implements IJoinComparisonOperator2<ET> {

	@Override
	protected IJoinCompoundCondition2<ET> nextForAbstractComparisonOperator() {
		return new JoinCompoundCondition2<ET>();
	}
}