package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.MinOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class MinOf1 extends SingleOperandFunction1<MinOf2> {

    public MinOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<MinOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<MinOf2>(new MinOf2(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
    }
}