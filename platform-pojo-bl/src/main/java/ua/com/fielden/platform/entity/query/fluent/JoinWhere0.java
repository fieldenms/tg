package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;

final class JoinWhere0<ET extends AbstractEntity<?>> //
		extends Where<IJoinComparisonOperator0<ET>, IJoinCompoundCondition0<ET>, IJoinWhere1<ET>, ET> //
		implements IJoinWhere0<ET> {

	public JoinWhere0(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	protected IJoinWhere1<ET> nextForWhere(final EqlSentenceBuilder builder) {
		return new JoinWhere1<ET>(builder);
	}

	@Override
	protected IJoinCompoundCondition0<ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
		return new JoinCompoundCondition0<ET>(builder);
	}

	@Override
	protected IJoinComparisonOperator0<ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
		return new JoinComparisonOperator0<ET>(builder);
	}

}
