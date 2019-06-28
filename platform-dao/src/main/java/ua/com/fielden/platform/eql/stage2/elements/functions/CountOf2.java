package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.CountOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class CountOf2 extends SingleOperandFunction2<CountOf3> {
    private final boolean distinct;

    public CountOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public TransformationResult<CountOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<CountOf3>(new CountOf3(operandTransformationResult.item, distinct), operandTransformationResult.updatedContext);
    }
}