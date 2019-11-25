package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.HourOf;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class HourOfBuilder extends OneArgumentFunctionBuilder {

    protected HourOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new HourOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
