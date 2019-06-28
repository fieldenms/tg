package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.math.BigDecimal;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.RoundTo3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class RoundTo2 extends TwoOperandsFunction2<RoundTo3> {

    public RoundTo2(final ISingleOperand2<? extends ISingleOperand3> operand1, final ISingleOperand2<? extends ISingleOperand3> operand2) {
        super(operand1, operand2);
    }

    @Override
    public Class type() {
        return BigDecimal.class;
    }
}