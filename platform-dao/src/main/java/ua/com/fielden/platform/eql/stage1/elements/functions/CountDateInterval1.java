package ua.com.fielden.platform.eql.stage1.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.CountDateInterval2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class CountDateInterval1 extends TwoOperandsFunction1<CountDateInterval2> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval1(final DateIntervalUnit intervalUnit, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> periodEndDate, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> periodStartDate) {
        super(periodEndDate, periodStartDate);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public TransformationResult<CountDateInterval2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult<CountDateInterval2>(new CountDateInterval2(intervalUnit, firstOperandTransformationResult.item, secondOperandTransformationResult.item), secondOperandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ((intervalUnit == null ? 0 : intervalUnit.hashCode()));
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