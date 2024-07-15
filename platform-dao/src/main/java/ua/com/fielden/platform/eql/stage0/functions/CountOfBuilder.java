package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.functions.CountOf1;

public class CountOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    public CountOfBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder, final boolean distinct) {
        super(parent, queryBuilder);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new CountOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
