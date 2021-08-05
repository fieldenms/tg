package ua.com.fielden.platform.eql.stage0.functions;

import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.operands.functions.YearOf1;

public class YearOfBuilder extends OneArgumentFunctionBuilder {

    public YearOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    Object getModel() {
        return new YearOf1(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
