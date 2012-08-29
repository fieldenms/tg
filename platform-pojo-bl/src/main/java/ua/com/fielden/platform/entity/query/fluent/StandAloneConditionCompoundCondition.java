package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

public class StandAloneConditionCompoundCondition extends AbstractLogicalCondition<IStandAloneConditionOperand> implements IStandAloneConditionCompoundCondition {

    protected StandAloneConditionCompoundCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public ConditionModel model() {
	return new ConditionModel(getTokens().getValues());
    }

    @Override
    IStandAloneConditionOperand getParent() {
	return new StandAloneConditionOperand(getTokens());
    }
}