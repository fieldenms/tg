package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.HourOf;

public class HourOfBuilder extends OneArgumentFunctionBuilder {

    protected HourOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new HourOf(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
