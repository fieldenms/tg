package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;

final class JoinWhere0<ET extends AbstractEntity<?>> //
		extends Where<IJoinComparisonOperator0<ET>, IJoinCompoundCondition0<ET>, IJoinWhere1<ET>, ET> //
		implements IJoinWhere0<ET> {

    public JoinWhere0(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IJoinWhere1<ET> nextForWhere(final Tokens tokens) {
		return new JoinWhere1<ET>(tokens);
	}

	@Override
	protected IJoinCompoundCondition0<ET> nextForConditionalOperand(final Tokens tokens) {
		return new JoinCompoundCondition0<ET>(tokens);
	}

	@Override
	protected IJoinComparisonOperator0<ET> nextForSingleOperand(final Tokens tokens) {
		return new JoinComparisonOperator0<ET>(tokens);
	}
}