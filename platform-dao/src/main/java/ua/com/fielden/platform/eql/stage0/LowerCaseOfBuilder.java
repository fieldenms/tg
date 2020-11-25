package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.LowerCaseOf1;

public class LowerCaseOfBuilder extends OneArgumentFunctionBuilder {

    protected LowerCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new LowerCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
