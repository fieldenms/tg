package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.functions.AverageOf1;

public class AverageOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    public AverageOfBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder, final boolean distinct) {
        super(parent, queryBuilder);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new AverageOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
