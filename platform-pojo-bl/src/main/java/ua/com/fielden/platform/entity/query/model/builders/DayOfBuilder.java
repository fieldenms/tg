package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.elements.DayOfModel;

public class DayOfBuilder extends AbstractFunctionBuilder {

    protected DayOfBuilder(final AbstractTokensBuilder parent) {
	super(parent);
    }

    @Override
    Object getModel() {
	return new DayOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
