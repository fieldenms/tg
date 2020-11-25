package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.MaxOf1;

public class MaxOfBuilder extends OneArgumentFunctionBuilder {

    protected MaxOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MaxOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
