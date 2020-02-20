package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.elements.AddDateInterval;

public class AddDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected AddDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
        return new AddDateInterval(getModelForSingleOperand(firstCat(), firstValue()), (DateIntervalUnit) secondValue(), getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
    }
}