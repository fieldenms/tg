package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.LowerCaseOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class LowerCaseOf1 extends SingleOperandFunction1<LowerCaseOf2> {
    public LowerCaseOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<LowerCaseOf2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(context);
        return new TransformationResult<LowerCaseOf2>(new LowerCaseOf2(operandTransformationResult.item), operandTransformationResult.updatedContext);
    }
}