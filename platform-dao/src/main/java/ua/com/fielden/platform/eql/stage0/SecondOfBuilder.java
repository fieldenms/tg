package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.SecondOf1;

public class SecondOfBuilder extends OneArgumentFunctionBuilder {

    protected SecondOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new SecondOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
