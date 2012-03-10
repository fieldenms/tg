package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.YearOf;

public class YearOfBuilder extends OneArgumentFunctionBuilder {

    protected YearOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new YearOf(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
