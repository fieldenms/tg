package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.DateOf1;

public class DateOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected DateOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new DateOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
