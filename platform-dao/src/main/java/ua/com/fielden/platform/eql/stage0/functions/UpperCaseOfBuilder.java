package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.operands.functions.UpperCaseOf1;

public class UpperCaseOfBuilder extends OneArgumentFunctionBuilder {

    public UpperCaseOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new UpperCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}