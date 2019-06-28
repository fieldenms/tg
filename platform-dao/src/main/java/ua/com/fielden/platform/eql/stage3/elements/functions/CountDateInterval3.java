package ua.com.fielden.platform.eql.stage3.elements.functions;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class CountDateInterval3 extends TwoOperandsFunction3 {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval3(final DateIntervalUnit intervalUnit, final ISingleOperand3 periodEndDate, final ISingleOperand3 periodStartDate) {
        super(periodEndDate, periodStartDate);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
    }
}