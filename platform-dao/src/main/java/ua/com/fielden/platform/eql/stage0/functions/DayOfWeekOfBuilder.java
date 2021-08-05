package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.operands.functions.DayOfWeekOf1;

public class DayOfWeekOfBuilder extends OneArgumentFunctionBuilder {

    public DayOfWeekOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new DayOfWeekOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}