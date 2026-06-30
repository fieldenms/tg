package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.MinuteOf3;

public class MinuteOf2 extends DatePartFunction2<MinuteOf3> {

    public MinuteOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public TransformationResultFromStage2To3<MinuteOf3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResultFromStage2To3<>(new MinuteOf3(operandTransformationResult.item, type), operandTransformationResult.updatedContext);
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