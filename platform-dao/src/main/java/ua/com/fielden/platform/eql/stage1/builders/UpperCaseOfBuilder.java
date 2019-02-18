package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.UpperCaseOf1;

public class UpperCaseOfBuilder extends OneArgumentFunctionBuilder {

    protected UpperCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new UpperCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
