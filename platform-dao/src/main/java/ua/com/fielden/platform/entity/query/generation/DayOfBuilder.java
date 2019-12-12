package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.DayOf;
import ua.com.fielden.platform.utils.IDates;

public class DayOfBuilder extends OneArgumentFunctionBuilder {

    protected DayOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new DayOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
