package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.CountDateInterval2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class CountDateInterval1 extends TwoOperandsFunction1<CountDateInterval2> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval1(final DateIntervalUnit intervalUnit, final ISingleOperand1<? extends ISingleOperand2<?>> periodEndDate, final ISingleOperand1<? extends ISingleOperand2<?>> periodStartDate) {
        super(periodEndDate, periodStartDate);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public TransformationResult<CountDateInterval2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult<? extends ISingleOperand2> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult<CountDateInterval2>(new CountDateInterval2(intervalUnit, firstOperandTransformationResult.item, secondOperandTransformationResult.item), secondOperandTransformationResult.updatedContext);
    }
}