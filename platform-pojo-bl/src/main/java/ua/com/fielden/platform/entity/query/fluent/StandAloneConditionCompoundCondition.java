package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

public class StandAloneConditionCompoundCondition<ET extends AbstractEntity<?>> extends AbstractLogicalCondition<IStandAloneConditionOperand<ET>> implements IStandAloneConditionCompoundCondition<ET> {

    protected StandAloneConditionCompoundCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public ConditionModel model() {
	return new ConditionModel(getTokens().getValues());
    }

    @Override
    IStandAloneConditionOperand<ET> getParent() {
	return new StandAloneConditionOperand<ET>(getTokens());
    }
}