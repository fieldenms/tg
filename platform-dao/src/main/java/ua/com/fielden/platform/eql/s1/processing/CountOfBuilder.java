package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.CountOf;



public class CountOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;
    protected CountOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues, final boolean distinct) {
	super(parent, queryBuilder, paramValues);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new CountOf(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
