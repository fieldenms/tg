package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.math.BigDecimal;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class AverageOf2 extends SingleOperandFunction2 {
    public final boolean distinct;

    public AverageOf2(final ISingleOperand2 operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public Class type() {
        return BigDecimal.class;
    }
}