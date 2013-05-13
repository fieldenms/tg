package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.AverageOf;

public class AverageOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;
    protected AverageOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean distinct) {
	super(parent, queryBuilder, paramValues);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new AverageOf(getModelForSingleOperand(firstCat(), firstValue()), distinct, getDbVersion());
    }
}
