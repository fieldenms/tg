package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.LowerCaseOf1;

public class LowerCaseOfBuilder1 extends OneArgumentFunctionBuilder1 {

    protected LowerCaseOfBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new LowerCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
