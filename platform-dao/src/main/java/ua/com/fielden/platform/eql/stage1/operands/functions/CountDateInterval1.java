package ua.com.fielden.platform.eql.stage1.operands.functions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.CountDateInterval2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class CountDateInterval1 extends TwoOperandsFunction1<CountDateInterval2> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval1(final DateIntervalUnit intervalUnit, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> periodEndDate, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> periodStartDate) {
        super(periodEndDate, periodStartDate);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public CountDateInterval2 transform(final TransformationContextFromStage1To2 context) {
        return new CountDateInterval2(intervalUnit, operand1.transform(context), operand2.transform(context));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + intervalUnit.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof CountDateInterval1)) {
            return false;
        }
        
        final CountDateInterval1 other = (CountDateInterval1) obj;
        
        return Objects.equals(intervalUnit, other.intervalUnit);
    }
}