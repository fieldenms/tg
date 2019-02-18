package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.MinuteOf1;

public class MinuteOfBuilder extends OneArgumentFunctionBuilder {

    protected MinuteOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MinuteOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
