package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.CountOf1;

public class CountOfBuilder1 extends OneArgumentFunctionBuilder1 {
    private final boolean distinct;

    protected CountOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final boolean distinct) {
        super(parent, queryBuilder);
        this.distinct = distinct;
    }

    @Override
    Object getModel() {
        return new CountOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
