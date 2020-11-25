package ua.com.fielden.platform.eql.stage3.functions;

import org.hibernate.type.IntegerType;

import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public abstract class DatePartFunction3 extends SingleOperandFunction3 {

    public DatePartFunction3(final ISingleOperand3 operand) {
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