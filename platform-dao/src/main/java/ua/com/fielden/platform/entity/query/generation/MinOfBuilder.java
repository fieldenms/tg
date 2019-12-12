package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MinOf;
import ua.com.fielden.platform.utils.IDates;

public class MinOfBuilder extends OneArgumentFunctionBuilder {

    protected MinOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new MinOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
