package ua.com.fielden.platform.eql.stage2.elements.functions;

import org.hibernate.type.IntegerType;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public abstract class DatePartFunction2<S3 extends ISingleOperand3> extends SingleOperandFunction2<S3> {

    public DatePartFunction2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        super(operand);
    }

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public Object hibType() {
        return IntegerType.INSTANCE;
    }
}