package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.AverageOf1;

public class AverageOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    protected AverageOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final boolean distinct) {
        super(parent, queryBuilder);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new AverageOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
