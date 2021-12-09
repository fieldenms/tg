package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.operands.functions.MonthOf1;

public class MonthOfBuilder extends OneArgumentFunctionBuilder {

    public MonthOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MonthOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
