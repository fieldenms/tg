package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.elements.functions.AddDateInterval1;

public class AddDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected AddDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new AddDateInterval1(getModelForSingleOperand(firstCat(), firstValue()), (DateIntervalUnit) secondValue(), getModelForSingleOperand(thirdCat(), thirdValue()));
    }
}