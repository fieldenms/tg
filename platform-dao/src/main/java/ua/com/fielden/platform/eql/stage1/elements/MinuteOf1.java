package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.MinuteOf2;

public class MinuteOf1 extends SingleOperandFunction1<MinuteOf2> {

    public MinuteOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<MinuteOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = getOperand().transform(resolutionContext);
        return new TransformationResult<MinuteOf2>(new MinuteOf2(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
    }
}