package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.CountOf;

public class CountOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;
    protected CountOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean distinct) {
	super(parent, queryBuilder, paramValues);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new CountOf(getModelForSingleOperand(firstCat(), firstValue()), distinct, getDbVersion());
    }
}
