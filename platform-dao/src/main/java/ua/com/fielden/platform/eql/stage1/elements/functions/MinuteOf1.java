package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.MinuteOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class MinuteOf1 extends SingleOperandFunction1<MinuteOf2> {

    public MinuteOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<MinuteOf2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<?>> operandTransformationResult = operand.transform(context);
        return new TransformationResult<MinuteOf2>(new MinuteOf2(operandTransformationResult.item), operandTransformationResult.updatedContext);
    }
}