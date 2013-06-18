package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.AverageOf1;

public class AverageOfBuilder1 extends OneArgumentFunctionBuilder1 {
    private final boolean distinct;
    protected AverageOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final boolean distinct) {
	super(parent, queryBuilder);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new AverageOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
