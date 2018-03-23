package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.DayOfWeekOf;

public class DayOfWeekOfBuilder extends OneArgumentFunctionBuilder {

    protected DayOfWeekOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
        return new DayOfWeekOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}