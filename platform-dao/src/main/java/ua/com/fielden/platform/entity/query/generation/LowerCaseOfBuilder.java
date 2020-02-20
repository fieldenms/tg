package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.LowerCaseOf;

public class LowerCaseOfBuilder extends OneArgumentFunctionBuilder {

    protected LowerCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
        return new LowerCaseOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
