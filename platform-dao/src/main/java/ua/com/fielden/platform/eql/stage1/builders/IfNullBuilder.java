package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.IfNull1;

public class IfNullBuilder extends TwoArgumentsFunctionBuilder {

    protected IfNullBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new IfNull1(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()));
    }
}