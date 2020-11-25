package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.MinOf1;

public class MinOfBuilder extends OneArgumentFunctionBuilder {

    protected MinOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MinOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
