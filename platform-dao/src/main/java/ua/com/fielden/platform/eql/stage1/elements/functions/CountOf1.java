package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.CountOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class CountOf1 extends SingleOperandFunction1<CountOf2> {
    private final boolean distinct;

    public CountOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public TransformationResult<CountOf2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<? extends ISingleOperand3>> operandTransformationResult = operand.transform(context);
        return new TransformationResult<CountOf2>(new CountOf2(operandTransformationResult.item, distinct), operandTransformationResult.updatedContext);
    }
}