package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.MaxOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class MaxOf1 extends SingleOperandFunction1<MaxOf2> {

    public MaxOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<MaxOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<MaxOf2>(new MaxOf2(operandTransformationResult.item), operandTransformationResult.updatedContext);
    }
}