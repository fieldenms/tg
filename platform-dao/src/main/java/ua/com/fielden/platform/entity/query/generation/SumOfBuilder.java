package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.SumOf;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class SumOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    protected SumOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean distinct, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new SumOf(getModelForSingleOperand(firstCat(), firstValue()), distinct, getDbVersion());
    }
}
