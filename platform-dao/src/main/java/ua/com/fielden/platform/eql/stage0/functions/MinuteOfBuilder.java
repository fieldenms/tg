package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.functions.MinuteOf1;

public class MinuteOfBuilder extends OneArgumentFunctionBuilder {

    public MinuteOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MinuteOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
