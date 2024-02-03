package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.functions.DayOfWeekOf1;

public class DayOfWeekOfBuilder extends OneArgumentFunctionBuilder {

    public DayOfWeekOfBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new DayOfWeekOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}