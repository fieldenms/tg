package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.LowerCaseOf;
import ua.com.fielden.platform.utils.IDates;

public class LowerCaseOfBuilder extends OneArgumentFunctionBuilder {

    protected LowerCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new LowerCaseOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
