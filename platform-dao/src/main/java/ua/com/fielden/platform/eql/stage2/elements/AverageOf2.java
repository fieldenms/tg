package ua.com.fielden.platform.eql.stage2.elements;

import java.math.BigDecimal;

public class AverageOf2 extends SingleOperandFunction2 {
    private final boolean distinct;

    public AverageOf2(final ISingleOperand2 operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public String type() {
        return BigDecimal.class.getName();
    }
}