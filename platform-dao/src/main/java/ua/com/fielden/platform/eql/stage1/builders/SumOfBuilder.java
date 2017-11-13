package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.SumOf1;

public class SumOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    protected SumOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final boolean distinct) {
        super(parent, queryBuilder);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new SumOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
