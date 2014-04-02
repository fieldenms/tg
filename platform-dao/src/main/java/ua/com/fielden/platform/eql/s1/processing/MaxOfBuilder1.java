package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.MaxOf1;

public class MaxOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected MaxOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new MaxOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
