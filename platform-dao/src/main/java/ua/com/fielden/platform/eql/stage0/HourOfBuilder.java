package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.HourOf1;

public class HourOfBuilder extends OneArgumentFunctionBuilder {

    protected HourOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new HourOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
