package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.MaxOf1;

public class MaxOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected MaxOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new MaxOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
