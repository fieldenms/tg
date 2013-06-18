package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.MinuteOf1;

public class MinuteOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected MinuteOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
	return new MinuteOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
