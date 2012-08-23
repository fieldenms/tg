package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.elements.CountDateInterval;

public class CountDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected CountDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new CountDateInterval((DateIntervalUnit) firstValue(), getModelForSingleOperand(secondCat(), secondValue()), getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
    }
}