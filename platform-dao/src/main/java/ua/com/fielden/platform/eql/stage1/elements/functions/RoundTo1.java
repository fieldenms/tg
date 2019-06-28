package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.RoundTo2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class RoundTo1 extends TwoOperandsFunction1<RoundTo2> {

    public RoundTo1(final ISingleOperand1<? extends ISingleOperand2<?>> operand1, final ISingleOperand1<? extends ISingleOperand2<?>> operand2) {
        super(operand1, operand2);
    }

    @Override
    public TransformationResult<RoundTo2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult<? extends ISingleOperand2> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult<RoundTo2>(new RoundTo2(firstOperandTransformationResult.item, secondOperandTransformationResult.item), secondOperandTransformationResult.updatedContext);
    }
}