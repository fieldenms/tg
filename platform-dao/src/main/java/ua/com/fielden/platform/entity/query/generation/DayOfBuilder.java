package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.DayOf;

public class DayOfBuilder extends OneArgumentFunctionBuilder {

    protected DayOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new DayOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
