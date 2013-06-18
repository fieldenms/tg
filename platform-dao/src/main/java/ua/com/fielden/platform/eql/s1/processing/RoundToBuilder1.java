package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.RoundTo1;

public class RoundToBuilder1 extends TwoArgumentsFunctionBuilder1 {

    protected RoundToBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
	return new RoundTo1(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()));
    }
}