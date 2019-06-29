package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.AverageOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class AverageOf1 extends SingleOperandFunction1<AverageOf2> {
    private final boolean distinct;

    public AverageOf1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public TransformationResult<AverageOf2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<?>> operandTransformationResult = operand.transform(context);
        return new TransformationResult<AverageOf2>(new AverageOf2(operandTransformationResult.item, distinct), operandTransformationResult.updatedContext);
    }
}