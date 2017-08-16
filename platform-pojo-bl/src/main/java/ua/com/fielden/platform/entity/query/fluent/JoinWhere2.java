package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinWhere2<ET extends AbstractEntity<?>> //
		extends Where<IJoinComparisonOperator2<ET>, IJoinCompoundCondition2<ET>, IJoinWhere3<ET>, ET> //
		implements IJoinWhere2<ET> {

	@Override
	protected IJoinWhere3<ET> nextForAbstractWhere() {
		return new JoinWhere3<ET>();
	}

	@Override
	protected IJoinCompoundCondition2<ET> nextForAbstractConditionalOperand() {
		return new JoinCompoundCondition2<ET>();
	}

	@Override
	protected IJoinComparisonOperator2<ET> nextForAbstractSingleOperand() {
		return new JoinComparisonOperator2<ET>();
	}
}