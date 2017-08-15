package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;

public class StandAloneConditionOperand<ET extends AbstractEntity<?>> //
extends AbstractWhereWithoutNesting<IStandAloneConditionComparisonOperator<ET>, IStandAloneConditionCompoundCondition<ET>, ET> //
implements IStandAloneConditionOperand<ET> {

    @Override
    IStandAloneConditionCompoundCondition<ET> getParent2() {
        return new StandAloneConditionCompoundCondition<ET>();
    }

    @Override
    IStandAloneConditionComparisonOperator<ET> getParent() {
        return new StandAloneConditionComparisonOperator<ET>();
    }
}