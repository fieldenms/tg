package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.AverageOf;
import ua.com.fielden.platform.utils.IDates;

public class AverageOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    protected AverageOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean distinct, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new AverageOf(getModelForSingleOperand(firstCat(), firstValue()), distinct, getDbVersion());
    }
}
