package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MonthOf;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class MonthOfBuilder extends OneArgumentFunctionBuilder {

    protected MonthOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new MonthOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
