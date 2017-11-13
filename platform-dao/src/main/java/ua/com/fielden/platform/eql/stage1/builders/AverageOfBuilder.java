package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.AverageOf1;

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
