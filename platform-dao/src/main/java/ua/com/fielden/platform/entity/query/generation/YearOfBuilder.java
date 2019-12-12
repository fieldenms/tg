package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.YearOf;
import ua.com.fielden.platform.utils.IDates;

public class YearOfBuilder extends OneArgumentFunctionBuilder {

    protected YearOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new YearOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
