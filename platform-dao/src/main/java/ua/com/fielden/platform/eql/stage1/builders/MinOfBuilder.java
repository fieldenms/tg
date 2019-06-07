package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.MinOf1;

public class MinOfBuilder extends OneArgumentFunctionBuilder {

    protected MinOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MinOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
