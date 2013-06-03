package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.DayOf1;


public class DayOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected DayOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new DayOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
