package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;

public class StandAloneConditionComparisonOperator<ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IStandAloneConditionCompoundCondition<ET>, ET> implements IStandAloneConditionComparisonOperator<ET> {

    @Override
    IStandAloneConditionCompoundCondition<ET> getParent1() {
        return new StandAloneConditionCompoundCondition<ET>();
    }

}