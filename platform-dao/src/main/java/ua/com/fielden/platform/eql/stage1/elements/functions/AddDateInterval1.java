package ua.com.fielden.platform.eql.stage1.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.AddDateInterval2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class AddDateInterval1 extends TwoOperandsFunction1<AddDateInterval2> {
    
    private final DateIntervalUnit intervalUnit;

    public AddDateInterval1(final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand1, final DateIntervalUnit intervalUnit, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand2) {
        super(operand1, operand2);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public TransformationResult<AddDateInterval2> transform(final PropsResolutionContext context, final String sourceId) {
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> firstOperandTr = operand1.transform(context, sourceId);
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> secondOperandTr = operand2.transform(firstOperandTr.updatedContext, sourceId);
        return new TransformationResult<AddDateInterval2>(new AddDateInterval2(firstOperandTr.item, intervalUnit, secondOperandTr.item), secondOperandTr.updatedContext);
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
        
        if (!(obj instanceof AddDateInterval1)) {
            return false;
        }
        
        final AddDateInterval1 other = (AddDateInterval1) obj;
        
        return Objects.equals(intervalUnit, other.intervalUnit);
    }
}