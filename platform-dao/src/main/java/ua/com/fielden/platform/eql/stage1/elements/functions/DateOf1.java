package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.DateOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class DateOf1 extends SingleOperandFunction1<DateOf2> {

    public DateOf1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        super(operand);
    }

    @Override
    public TransformationResult<DateOf2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = getOperand().transform(resolutionContext);
        return new TransformationResult<DateOf2>(new DateOf2(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
    }
}