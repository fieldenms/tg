package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.MaxOf1;

public class MaxOfBuilder extends OneArgumentFunctionBuilder {

    protected MaxOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MaxOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
