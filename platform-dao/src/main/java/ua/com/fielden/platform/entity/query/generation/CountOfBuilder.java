package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.CountOfModel;

public class CountOfBuilder extends AbstractFunctionBuilder {
    private final boolean distinct;
    protected CountOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean distinct) {
	super(parent, queryBuilder, paramValues);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new CountOfModel(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
