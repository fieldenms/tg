package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.eql.s1.elements.IfNull1;

public class IfNullBuilder1 extends TwoArgumentsFunctionBuilder1 {

    protected IfNullBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
	return new IfNull1(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()));
    }
}