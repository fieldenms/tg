package ua.com.fielden.platform.eql.stage1.builders;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.elements.AddDateInterval;

public class AddDateIntervalBuilder extends ThreeArgumentsFunctionBuilder {

    protected AddDateIntervalBuilder(AbstractTokensBuilder parent, EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
        // TODO Auto-generated constructor stub
    }

    @Override
    Object getModel() {
        // TODO Auto-generated method stub
        return null;
    }

//    protected AddDateIntervalBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
//        super(parent, queryBuilder, paramValues);
//    }
//
//    @Override
//    Object getModel() {
//        return new AddDateInterval(getModelForSingleOperand(firstCat(), firstValue()), (DateIntervalUnit) secondValue(), getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
//    }
}