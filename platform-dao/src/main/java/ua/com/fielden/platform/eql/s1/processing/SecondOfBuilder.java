package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.SecondOf;

public class SecondOfBuilder extends OneArgumentFunctionBuilder {

    protected SecondOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new SecondOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
