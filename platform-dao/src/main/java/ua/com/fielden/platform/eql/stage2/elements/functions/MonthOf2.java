package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.MonthOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class MonthOf2 extends SingleOperandFunction2<MonthOf3> {

    public MonthOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public TransformationResult<MonthOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<MonthOf3>(new MonthOf3(operandTransformationResult.item), operandTransformationResult.updatedContext);
    }
}