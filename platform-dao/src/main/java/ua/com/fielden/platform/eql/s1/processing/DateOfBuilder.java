package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.DateOf;


public class DateOfBuilder extends OneArgumentFunctionBuilder {

    protected DateOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new DateOf(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
