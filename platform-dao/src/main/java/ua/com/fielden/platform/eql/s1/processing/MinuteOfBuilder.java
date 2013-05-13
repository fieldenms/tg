package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.MinuteOf;

public class MinuteOfBuilder extends OneArgumentFunctionBuilder {

    protected MinuteOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new MinuteOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
    }
}
