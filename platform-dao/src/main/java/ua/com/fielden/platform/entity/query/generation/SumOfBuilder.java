package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.SumOf;

public class SumOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;
    protected SumOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean distinct) {
	super(parent, queryBuilder, paramValues);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new SumOf(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
