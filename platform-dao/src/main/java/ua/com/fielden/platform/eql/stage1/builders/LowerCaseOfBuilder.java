package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.LowerCaseOf1;

public class LowerCaseOfBuilder extends OneArgumentFunctionBuilder {

    protected LowerCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new LowerCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
