package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.RoundTo3;

public class RoundTo2 extends TwoOperandsFunction2<RoundTo3> {

    public RoundTo2(final ISingleOperand2<? extends ISingleOperand3> operand1, final ISingleOperand2<? extends ISingleOperand3> operand2) {
        super(operand1, operand2, operand1.type());
    }

    @Override
    public TransformationResult2<RoundTo3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISingleOperand3> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult2<? extends ISingleOperand3> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult2<>(new RoundTo3(firstOperandTransformationResult.item, secondOperandTransformationResult.item, type), secondOperandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + RoundTo2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof RoundTo2;
    }
}