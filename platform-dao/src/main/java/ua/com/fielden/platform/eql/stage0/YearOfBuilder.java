package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.eql.stage1.functions.YearOf1;

public class YearOfBuilder extends OneArgumentFunctionBuilder {

    protected YearOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new YearOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
