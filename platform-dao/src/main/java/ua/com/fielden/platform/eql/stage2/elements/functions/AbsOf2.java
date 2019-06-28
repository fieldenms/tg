package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.AbsOf3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class AbsOf2 extends SingleOperandFunction2<AbsOf3> {

    public AbsOf2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public Class type() {
        return operand.type();
    }
}