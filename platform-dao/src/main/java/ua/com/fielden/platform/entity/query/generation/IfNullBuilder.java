package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.IfNull;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class IfNullBuilder extends TwoArgumentsFunctionBuilder {

    protected IfNullBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new IfNull(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()), getDbVersion());
    }
}