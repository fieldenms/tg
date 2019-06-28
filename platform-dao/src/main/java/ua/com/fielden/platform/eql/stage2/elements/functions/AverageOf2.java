package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.math.BigDecimal;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.AverageOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class AverageOf2 extends SingleOperandFunction2<AverageOf3> {
    public final boolean distinct;

    public AverageOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public Class type() {
        return BigDecimal.class;
    }

    @Override
    public TransformationResult<AverageOf3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<AverageOf3>(new AverageOf3(operandTransformationResult.item, distinct), operandTransformationResult.updatedContext);
    }
}