package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.RoundTo1;

public class RoundToBuilder extends TwoArgumentsFunctionBuilder {

    protected RoundToBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new RoundTo1(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()));
    }
}