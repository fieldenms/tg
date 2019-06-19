package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public abstract class AbstractFunction2 implements ISingleOperand2 {

    @Override
    public boolean ignore() {
        return false;
    }
}