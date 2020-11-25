package ua.com.fielden.platform.eql.stage2.functions;

import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public abstract class AbstractFunction2<S3 extends ISingleOperand3> implements ISingleOperand2<S3> {

    @Override
    public boolean ignore() {
        return false;
    }
}