package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;
import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class CountDateInterval extends TwoOperandsFunction {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval(final DateIntervalUnit intervalUnit, final ISingleOperand periodEndDate, final ISingleOperand periodStartDate, final DbVersion dbVersion) {
	super(dbVersion, periodEndDate, periodStartDate);
	this.intervalUnit = intervalUnit;
    }
}