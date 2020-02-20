package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.SecondOf;

public class SecondOfBuilder extends OneArgumentFunctionBuilder {

    protected SecondOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
        return new SecondOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
