package ua.com.fielden.platform.eql.stage2.operands.functions;

import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.CountDateInterval3;

public class CountDateInterval2 extends TwoOperandsFunction2<CountDateInterval3> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval2(final DateIntervalUnit intervalUnit, final ISingleOperand2<? extends ISingleOperand3> periodEndDate, final ISingleOperand2<? extends ISingleOperand3> periodStartDate) {
        super(periodEndDate, periodStartDate, INTEGER_PROP_TYPE);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public TransformationResult2<CountDateInterval3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISingleOperand3> firstOperandTr = operand1.transform(context);
        final TransformationResult2<? extends ISingleOperand3> secondOperandTr = operand2.transform(firstOperandTr.updatedContext);
        return new TransformationResult2<>(new CountDateInterval3(intervalUnit, firstOperandTr.item, secondOperandTr.item, type), secondOperandTr.updatedContext);
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
        
        if (!(obj instanceof CountDateInterval2)) {
            return false;
        }
        
        final CountDateInterval2 other = (CountDateInterval2) obj;
        
        return Objects.equals(intervalUnit, other.intervalUnit);
    }
}