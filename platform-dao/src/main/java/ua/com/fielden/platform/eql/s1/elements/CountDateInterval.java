package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;


public class CountDateInterval extends TwoOperandsFunction<ua.com.fielden.platform.eql.s2.elements.CountDateInterval> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval(final DateIntervalUnit intervalUnit, final ISingleOperand<? extends ISingleOperand2> periodEndDate, final ISingleOperand<? extends ISingleOperand2> periodStartDate) {
	super(periodEndDate, periodStartDate);
	this.intervalUnit = intervalUnit;
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.CountDateInterval transform() {
	return new ua.com.fielden.platform.eql.s2.elements.CountDateInterval(intervalUnit, getOperand1().transform(), getOperand2().transform());
    }
}