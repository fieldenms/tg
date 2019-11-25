package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.RoundTo;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class RoundToBuilder extends TwoArgumentsFunctionBuilder {

    protected RoundToBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IUniversalConstants universalConstants) {
        super(parent, queryBuilder, paramValues, universalConstants);
    }

    @Override
    Object getModel() {
        return new RoundTo(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()), getDbVersion());
    }
}