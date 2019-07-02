package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.DayOfWeekOf1;

public class DayOfWeekOfBuilder extends OneArgumentFunctionBuilder {

    protected DayOfWeekOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new DayOfWeekOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}