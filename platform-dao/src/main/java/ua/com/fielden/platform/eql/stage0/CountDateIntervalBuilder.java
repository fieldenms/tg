package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.functions.CountDateInterval1;

public class CountDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected CountDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new CountDateInterval1((DateIntervalUnit) firstValue(), getModelForSingleOperand(secondCat(), secondValue()), getModelForSingleOperand(thirdCat(), thirdValue()));
    }
}