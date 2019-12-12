package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.SecondOf;
import ua.com.fielden.platform.utils.IDates;

public class SecondOfBuilder extends OneArgumentFunctionBuilder {

    protected SecondOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new SecondOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
