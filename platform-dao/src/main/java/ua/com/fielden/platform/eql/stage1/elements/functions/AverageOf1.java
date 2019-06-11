package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.AverageOf2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class AverageOf1 extends SingleOperandFunction1<AverageOf2> {
    private final boolean distinct;

    public AverageOf1(final ISingleOperand1<? extends ISingleOperand2> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public TransformationResult<AverageOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = getOperand().transform(resolutionContext);
        return new TransformationResult<AverageOf2>(new AverageOf2(operandTransformationResult.getItem(), distinct), operandTransformationResult.getUpdatedContext());
    }
}