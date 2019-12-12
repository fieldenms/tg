package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.UpperCaseOf;
import ua.com.fielden.platform.utils.IDates;

public class UpperCaseOfBuilder extends OneArgumentFunctionBuilder {

    protected UpperCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new UpperCaseOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
