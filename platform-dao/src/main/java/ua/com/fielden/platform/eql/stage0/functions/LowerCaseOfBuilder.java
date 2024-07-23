package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.functions.LowerCaseOf1;

public class LowerCaseOfBuilder extends OneArgumentFunctionBuilder {

    public LowerCaseOfBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new LowerCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
