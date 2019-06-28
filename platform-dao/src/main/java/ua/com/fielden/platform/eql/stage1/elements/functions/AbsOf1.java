package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.AbsOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class AbsOf1 extends SingleOperandFunction1<AbsOf2> {

    public AbsOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<AbsOf2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(context);
        return new TransformationResult<AbsOf2>(new AbsOf2(operandTransformationResult.item), operandTransformationResult.updatedContext);
    }
}