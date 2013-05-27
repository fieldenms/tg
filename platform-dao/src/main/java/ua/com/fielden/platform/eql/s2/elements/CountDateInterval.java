package ua.com.fielden.platform.eql.s2.elements;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;


public class CountDateInterval extends TwoOperandsFunction {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval(final DateIntervalUnit intervalUnit, final ISingleOperand2 periodEndDate, final ISingleOperand2 periodStartDate) {
	super(periodEndDate, periodStartDate);
	this.intervalUnit = intervalUnit;
    }
}