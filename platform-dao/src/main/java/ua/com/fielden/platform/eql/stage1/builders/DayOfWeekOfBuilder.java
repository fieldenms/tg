package ua.com.fielden.platform.eql.stage1.builders;

import java.util.Map;

import ua.com.fielden.platform.eql.stage1.elements.functions.DayOfWeekOf;

public class DayOfWeekOfBuilder extends OneArgumentFunctionBuilder {

    protected DayOfWeekOfBuilder(AbstractTokensBuilder parent, EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
        // TODO Auto-generated constructor stub
    }

    @Override
    Object getModel() {
        // TODO Auto-generated method stub
        return null;
    }

//    protected DayOfWeekOfBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
//        super(parent, queryBuilder, paramValues);
//    }
//
//    @Override
//    Object getModel() {
//        return new DayOfWeekOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
//    }
}