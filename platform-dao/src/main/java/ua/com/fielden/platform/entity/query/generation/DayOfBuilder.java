package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.DayOfModel;

public class DayOfBuilder extends AbstractFunctionBuilder {

    protected DayOfBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    Object getModel() {
	return new DayOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
