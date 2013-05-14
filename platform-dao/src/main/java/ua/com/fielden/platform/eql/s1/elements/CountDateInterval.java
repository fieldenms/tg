package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;


public class CountDateInterval extends TwoOperandsFunction {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval(final DateIntervalUnit intervalUnit, final ISingleOperand periodEndDate, final ISingleOperand periodStartDate) {
	super(periodEndDate, periodStartDate);
	this.intervalUnit = intervalUnit;
    }
}