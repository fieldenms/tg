package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.SumOf1;

public class SumOfBuilder1 extends OneArgumentFunctionBuilder1 {
    private final boolean distinct;
    protected SumOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues, final boolean distinct) {
	super(parent, queryBuilder, paramValues);
	this.distinct = distinct;
    }

    @Override
    Object getModel() {
	return new SumOf1(getModelForSingleOperand(firstCat(), firstValue()), distinct);
    }
}
