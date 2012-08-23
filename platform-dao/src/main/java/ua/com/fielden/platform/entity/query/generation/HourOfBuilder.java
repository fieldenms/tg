package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.HourOf;

public class HourOfBuilder extends OneArgumentFunctionBuilder {

    protected HourOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new HourOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
