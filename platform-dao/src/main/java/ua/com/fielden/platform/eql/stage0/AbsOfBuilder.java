package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.AbsOf1;

public class AbsOfBuilder extends OneArgumentFunctionBuilder {

    protected AbsOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new AbsOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
