package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.AverageOfModel;

public class AverageOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;
    protected AverageOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean distinct) {
	super(parent, queryBuilder, paramValues);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new AverageOfModel(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
