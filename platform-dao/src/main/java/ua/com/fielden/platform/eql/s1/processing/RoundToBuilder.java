package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.RoundTo;

public class RoundToBuilder extends TwoArgumentsFunctionBuilder {

    protected RoundToBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    Object getModel() {
	return new RoundTo(getModelForSingleOperand(firstCat(), firstValue()), getModelForSingleOperand(secondCat(), secondValue()), getDbVersion());
    }
}