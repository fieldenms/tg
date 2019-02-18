package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.SecondOf1;

public class SecondOfBuilder extends OneArgumentFunctionBuilder {

    protected SecondOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new SecondOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
