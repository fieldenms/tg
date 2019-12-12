package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.elements.AddDateInterval;
import ua.com.fielden.platform.utils.IDates;

public class AddDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected AddDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new AddDateInterval(getModelForSingleOperand(firstCat(), firstValue()), (DateIntervalUnit) secondValue(), getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
    }
}