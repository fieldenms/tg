package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.eql.stage1.elements.functions.DateOf1;

public class DateOfBuilder extends OneArgumentFunctionBuilder {

    protected DateOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new DateOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
