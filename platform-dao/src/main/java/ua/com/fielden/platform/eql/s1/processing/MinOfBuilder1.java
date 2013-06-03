package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.MinOf1;

public class MinOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected MinOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new MinOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
