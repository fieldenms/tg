package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MaxOf;
import ua.com.fielden.platform.utils.IDates;

public class MaxOfBuilder extends OneArgumentFunctionBuilder {

    protected MaxOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new MaxOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
