package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.functions.SumOf1;

public class SumOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    public SumOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final boolean distinct) {
        super(parent, queryBuilder);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new SumOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
