package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.MinuteOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class MinuteOf2 extends SingleOperandFunction2<MinuteOf3> {

    public MinuteOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public TransformationResult<MinuteOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<MinuteOf3>(new MinuteOf3(operandTransformationResult.item), operandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + MinuteOf2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof MinuteOf2;
    } 
}