package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.DateOf;
import ua.com.fielden.platform.utils.IDates;

public class DateOfBuilder extends OneArgumentFunctionBuilder {

    protected DateOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new DateOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
