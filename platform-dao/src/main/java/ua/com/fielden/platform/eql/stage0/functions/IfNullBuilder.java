package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.functions.IfNull1;

public class IfNullBuilder extends TwoArgumentsFunctionBuilder {

    public IfNullBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new IfNull1(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()));
    }
}