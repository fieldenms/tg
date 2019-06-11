package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.functions.CountDateInterval2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class CountDateInterval1 extends TwoOperandsFunction1<CountDateInterval2> {

    private DateIntervalUnit intervalUnit;

    public CountDateInterval1(final DateIntervalUnit intervalUnit, final ISingleOperand1<? extends ISingleOperand2> periodEndDate, final ISingleOperand1<? extends ISingleOperand2> periodStartDate) {
        super(periodEndDate, periodStartDate);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public TransformationResult<CountDateInterval2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> firstOperandTransformationResult = getOperand1().transform(resolutionContext);
        final TransformationResult<? extends ISingleOperand2> secondOperandTransformationResult = getOperand2().transform(firstOperandTransformationResult.getUpdatedContext());
        return new TransformationResult<CountDateInterval2>(new CountDateInterval2(intervalUnit, firstOperandTransformationResult.getItem(), secondOperandTransformationResult.getItem()), secondOperandTransformationResult.getUpdatedContext());
    }
}