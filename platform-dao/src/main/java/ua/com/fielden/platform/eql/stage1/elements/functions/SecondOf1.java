package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.SecondOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class SecondOf1 extends SingleOperandFunction1<SecondOf2> {

    public SecondOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<SecondOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<SecondOf2>(new SecondOf2(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
    }
}