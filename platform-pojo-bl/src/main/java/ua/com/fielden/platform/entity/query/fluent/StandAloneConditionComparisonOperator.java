package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;

public final class StandAloneConditionComparisonOperator<ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IStandAloneConditionCompoundCondition<ET>, ET> //
		implements IStandAloneConditionComparisonOperator<ET> {

    public StandAloneConditionComparisonOperator(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IStandAloneConditionCompoundCondition<ET> nextForComparisonOperator(final Tokens tokens) {
		return new StandAloneConditionCompoundCondition<ET>(tokens);
	}

}