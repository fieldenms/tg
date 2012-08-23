package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.UpperCaseOf;

public class UpperCaseOfBuilder extends OneArgumentFunctionBuilder {

    protected UpperCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new UpperCaseOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
