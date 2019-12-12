package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.MonthOf;
import ua.com.fielden.platform.utils.IDates;

public class MonthOfBuilder extends OneArgumentFunctionBuilder {

    protected MonthOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new MonthOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
