package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.ConcatOfOrderItem2;

public record ConcatOfOrderItem1(
        ISingleOperand1<? extends ISingleOperand2<?>> operand,
        boolean isDesc)
{
    public ConcatOfOrderItem2 transform(final TransformationContextFromStage1To2 context) {
        return new ConcatOfOrderItem2(operand.transform(context), isDesc);
    }
}
