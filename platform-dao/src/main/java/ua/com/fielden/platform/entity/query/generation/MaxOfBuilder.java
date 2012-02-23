package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MaxOfModel;

public class MaxOfBuilder extends AbstractFunctionBuilder {

    protected MaxOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new MaxOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
