package ua.com.fielden.platform.eql.s2.elements;

import java.math.BigDecimal;

public class RoundTo2 extends TwoOperandsFunction2 {

    public RoundTo2(final ISingleOperand2 operand1, final ISingleOperand2 operand2) {
        super(operand1, operand2);
    }

    @Override
    public Class type() {
        return BigDecimal.class;
    }
}