package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.MinOf;

public class MinOfBuilder extends OneArgumentFunctionBuilder {

    protected MinOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new MinOf(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
