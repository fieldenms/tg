package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.YearOf2;

public class YearOf1 extends SingleOperandFunction1<YearOf2> {

    public YearOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<YearOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = getOperand().transform(resolutionContext);
        return new TransformationResult<YearOf2>(new YearOf2(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
    }
}