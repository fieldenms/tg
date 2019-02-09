package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.IfNull2;

public class IfNull1 extends TwoOperandsFunction1<IfNull2> {

    public IfNull1(final ISingleOperand1<? extends ISingleOperand2> operand1, final ISingleOperand1<? extends ISingleOperand2> operand2) {
        super(operand1, operand2);
    }

    @Override
    public TransformationResult<IfNull2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> firstOperandTransformationResult = getOperand1().transform(resolutionContext);
        final TransformationResult<? extends ISingleOperand2> secondOperandTransformationResult = getOperand2().transform(firstOperandTransformationResult.getUpdatedContext());
        return new TransformationResult<IfNull2>(new IfNull2(firstOperandTransformationResult.getItem(), secondOperandTransformationResult.getItem()), secondOperandTransformationResult.getUpdatedContext());
    }
}