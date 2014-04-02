package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.fluent.DateIntervalUnit;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.CountDateInterval2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;

public class CountDateInterval1 extends TwoOperandsFunction1<CountDateInterval2> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval1(final DateIntervalUnit intervalUnit, final ISingleOperand1<? extends ISingleOperand2> periodEndDate, final ISingleOperand1<? extends ISingleOperand2> periodStartDate) {
        super(periodEndDate, periodStartDate);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public CountDateInterval2 transform(final TransformatorToS2 resolver) {
        return new CountDateInterval2(intervalUnit, getOperand1().transform(resolver), getOperand2().transform(resolver));
    }
}