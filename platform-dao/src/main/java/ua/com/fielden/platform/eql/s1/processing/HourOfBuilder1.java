package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.HourOf1;

public class HourOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected HourOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new HourOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
