package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.CountDateInterval3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class CountDateInterval2 extends TwoOperandsFunction2<CountDateInterval3> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval2(final DateIntervalUnit intervalUnit, final ISingleOperand2<? extends ISingleOperand3> periodEndDate, final ISingleOperand2<? extends ISingleOperand3> periodStartDate) {
        super(periodEndDate, periodStartDate);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public Class type() {
        return BigDecimal.class;
    }
}