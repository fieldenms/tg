package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.SumOf2;

public class SumOf1 extends SingleOperandFunction1<SumOf2> {
    private final boolean distinct;

    public SumOf1(final ISingleOperand1<? extends ISingleOperand2> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public TransformationResult<SumOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = getOperand().transform(resolutionContext);
        return new TransformationResult<SumOf2>(new SumOf2(operandTransformationResult.getItem(), distinct), operandTransformationResult.getUpdatedContext());
    }
}