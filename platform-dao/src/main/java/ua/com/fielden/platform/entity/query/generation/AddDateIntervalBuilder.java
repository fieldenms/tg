package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.elements.AddDateInterval;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class AddDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected AddDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new AddDateInterval(getModelForSingleOperand(firstCat(), firstValue()), (DateIntervalUnit) secondValue(), getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
    }
}