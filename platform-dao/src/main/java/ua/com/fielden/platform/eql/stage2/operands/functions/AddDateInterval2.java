package ua.com.fielden.platform.eql.stage2.operands.functions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.AddDateInterval3;

public class AddDateInterval2 extends TwoOperandsFunction2<AddDateInterval3> {
    private final DateIntervalUnit intervalUnit;
    
    public AddDateInterval2(final ISingleOperand2<? extends ISingleOperand3> operand1, final DateIntervalUnit intervalUnit, final ISingleOperand2<? extends ISingleOperand3> operand2) {
        super(operand1, operand2, operand2.type(), operand2.hibType());
        this.intervalUnit = intervalUnit;
    }

    @Override
    public TransformationResult<AddDateInterval3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> firstOperandTr = operand1.transform(context);
        final TransformationResult<? extends ISingleOperand3> secondOperandTr = operand2.transform(firstOperandTr.updatedContext);
        return new TransformationResult<>(new AddDateInterval3(firstOperandTr.item, intervalUnit, secondOperandTr.item, type, hibType), secondOperandTr.updatedContext);
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
        
        if (!(obj instanceof AddDateInterval2)) {
            return false;
        }
        
        final AddDateInterval2 other = (AddDateInterval2) obj;
        
        return Objects.equals(intervalUnit, other.intervalUnit);
    }
}