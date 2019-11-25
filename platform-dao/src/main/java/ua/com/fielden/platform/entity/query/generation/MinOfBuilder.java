package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MinOf;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class MinOfBuilder extends OneArgumentFunctionBuilder {

    protected MinOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new MinOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
