package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.YearOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class YearOf2 extends SingleOperandFunction2<YearOf3> {

    public YearOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public Class type() {
        return Integer.class;
    }
}