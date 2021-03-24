package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.functions.CountDateInterval1;

public class CountDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    public CountDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new CountDateInterval1((DateIntervalUnit) firstValue(), getModelForSingleOperand(secondCat(), secondValue()), getModelForSingleOperand(thirdCat(), thirdValue()));
    }
}