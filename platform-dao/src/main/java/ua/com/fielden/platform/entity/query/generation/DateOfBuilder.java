package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.DateOf;

public class DateOfBuilder extends OneArgumentFunctionBuilder {

    protected DateOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
        return new DateOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
