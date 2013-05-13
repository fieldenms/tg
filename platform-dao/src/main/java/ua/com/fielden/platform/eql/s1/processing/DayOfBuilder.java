package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.DayOf;


public class DayOfBuilder extends OneArgumentFunctionBuilder {

    protected DayOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new DayOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
