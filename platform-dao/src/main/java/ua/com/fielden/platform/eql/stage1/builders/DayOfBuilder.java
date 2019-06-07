package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.DayOf1;

public class DayOfBuilder extends OneArgumentFunctionBuilder {

    protected DayOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new DayOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
