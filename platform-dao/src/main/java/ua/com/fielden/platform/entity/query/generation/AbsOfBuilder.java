package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.AbsOf;
import ua.com.fielden.platform.utils.IDates;

public class AbsOfBuilder extends OneArgumentFunctionBuilder {

    protected AbsOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new AbsOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
