package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.MonthOf1;

public class MonthOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected MonthOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
	return new MonthOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
