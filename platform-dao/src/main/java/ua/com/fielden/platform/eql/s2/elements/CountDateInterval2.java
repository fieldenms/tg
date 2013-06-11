package ua.com.fielden.platform.eql.s2.elements;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;


public class CountDateInterval2 extends TwoOperandsFunction2 {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval2(final DateIntervalUnit intervalUnit, final ISingleOperand2 periodEndDate, final ISingleOperand2 periodStartDate) {
	super(periodEndDate, periodStartDate);
	this.intervalUnit = intervalUnit;
    }

    @Override
    public Class type() {
	return BigDecimal.class;
    }
}