package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.MonthOf2;

public class MonthOf1 extends SingleOperandFunction1<MonthOf2> {

    public MonthOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<MonthOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = getOperand().transform(resolutionContext);
        return new TransformationResult<MonthOf2>(new MonthOf2(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
    }
}