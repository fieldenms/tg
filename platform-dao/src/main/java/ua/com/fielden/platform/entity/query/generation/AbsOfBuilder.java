package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.AbsOf;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class AbsOfBuilder extends OneArgumentFunctionBuilder {

    protected AbsOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new AbsOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
