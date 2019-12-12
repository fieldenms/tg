package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.HourOf;
import ua.com.fielden.platform.utils.IDates;

public class HourOfBuilder extends OneArgumentFunctionBuilder {

    protected HourOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new HourOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
