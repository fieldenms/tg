package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.MonthOf1;

public class MonthOfBuilder extends OneArgumentFunctionBuilder {

    protected MonthOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MonthOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
