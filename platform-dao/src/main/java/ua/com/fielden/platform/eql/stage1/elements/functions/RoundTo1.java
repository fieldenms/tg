package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.RoundTo2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class RoundTo1 extends TwoOperandsFunction1<RoundTo2> {

    public RoundTo1(final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand1, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand2) {
        super(operand1, operand2);
    }

    @Override
    public TransformationResult<RoundTo2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult<RoundTo2>(new RoundTo2(firstOperandTransformationResult.item, secondOperandTransformationResult.item), secondOperandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + RoundTo1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof RoundTo1;
    } 
}