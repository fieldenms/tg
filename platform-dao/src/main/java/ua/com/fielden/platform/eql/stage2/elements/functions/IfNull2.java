package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.IfNull3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class IfNull2 extends TwoOperandsFunction2<IfNull3> {

    public IfNull2(final ISingleOperand2<? extends ISingleOperand3> operand1, final ISingleOperand2<? extends ISingleOperand3> operand2) {
        super(operand1, operand2);
    }

    @Override
    public Class<?> type() {
        return operand1.type(); //TODO
    }

    @Override
    public TransformationResult<IfNull3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> firstOperandTransformationResult = operand1.transform(context);
        final TransformationResult<? extends ISingleOperand3> secondOperandTransformationResult = operand2.transform(firstOperandTransformationResult.updatedContext);
        return new TransformationResult<IfNull3>(new IfNull3(firstOperandTransformationResult.item, secondOperandTransformationResult.item), secondOperandTransformationResult.updatedContext);
    }
}