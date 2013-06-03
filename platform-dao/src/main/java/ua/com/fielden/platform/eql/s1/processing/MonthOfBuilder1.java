package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.MonthOf1;

public class MonthOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected MonthOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new MonthOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
