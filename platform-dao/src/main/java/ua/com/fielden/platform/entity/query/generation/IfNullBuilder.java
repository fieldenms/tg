package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.generation.elements.IfNull;
import ua.com.fielden.platform.utils.IDates;

public class IfNullBuilder extends TwoArgumentsFunctionBuilder {

    protected IfNullBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    Object getModel() {
        return new IfNull(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()), getDbVersion());
    }
}