package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.elements.CountDateInterval;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class CountDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected CountDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new CountDateInterval((DateIntervalUnit) firstValue(), getModelForSingleOperand(secondCat(), secondValue()), getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
    }
}