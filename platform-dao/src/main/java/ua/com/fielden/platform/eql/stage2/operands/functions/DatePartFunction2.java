package ua.com.fielden.platform.eql.stage2.operands.functions;

import org.hibernate.type.IntegerType;

import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public abstract class DatePartFunction2<S3 extends ISingleOperand3> extends SingleOperandFunction2<S3> {

    public DatePartFunction2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand, Integer.class, IntegerType.INSTANCE);
    }
}