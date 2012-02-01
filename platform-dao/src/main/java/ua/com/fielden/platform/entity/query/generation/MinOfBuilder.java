package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MonthOfModel;

public class MinOfBuilder extends AbstractFunctionBuilder {

    protected MinOfBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    Object getModel() {
	return new MonthOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
