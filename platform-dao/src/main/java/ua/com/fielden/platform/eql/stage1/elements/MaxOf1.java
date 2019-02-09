package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.MaxOf2;

public class MaxOf1 extends SingleOperandFunction1<MaxOf2> {

    public MaxOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<MaxOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = getOperand().transform(resolutionContext);
        return new TransformationResult<MaxOf2>(new MaxOf2(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
    }
}