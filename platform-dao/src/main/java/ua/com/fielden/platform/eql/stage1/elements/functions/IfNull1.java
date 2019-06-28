package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.IfNull2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class IfNull1 extends TwoOperandsFunction1<IfNull2> {

    public IfNull1(final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand1, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand2) {
        super(operand1, operand2);
    }

    @Override
    public TransformationResult<IfNull2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult<IfNull2>(new IfNull2(firstOperandTransformationResult.item, secondOperandTransformationResult.item), secondOperandTransformationResult.updatedContext);
    }
}