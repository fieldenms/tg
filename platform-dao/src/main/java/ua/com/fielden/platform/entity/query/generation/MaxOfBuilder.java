package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MaxOf;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class MaxOfBuilder extends OneArgumentFunctionBuilder {

    protected MaxOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new MaxOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
