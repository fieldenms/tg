package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.CountOf1;

public class CountOfBuilder extends OneArgumentFunctionBuilder {
    private final boolean distinct;

    protected CountOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final boolean distinct) {
        super(parent, queryBuilder);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new CountOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
