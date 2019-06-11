package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.math.BigDecimal;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class SumOf2 extends SingleOperandFunction2 {
    private final boolean distinct;

    public SumOf2(final ISingleOperand2 operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public Class type() {
        return BigDecimal.class;
    }
}