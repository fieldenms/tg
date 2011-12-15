package ua.com.fielden.platform.entity.query.model.builders;

import java.util.Map;

import ua.com.fielden.platform.entity.query.model.elements.DayOfModel;

public class DayOfBuilder extends AbstractFunctionBuilder {

    protected DayOfBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    Object getModel() {
	return new DayOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
