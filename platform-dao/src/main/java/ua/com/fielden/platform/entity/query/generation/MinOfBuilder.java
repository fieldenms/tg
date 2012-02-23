package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MinOfModel;

public class MinOfBuilder extends AbstractFunctionBuilder {

    protected MinOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new MinOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
