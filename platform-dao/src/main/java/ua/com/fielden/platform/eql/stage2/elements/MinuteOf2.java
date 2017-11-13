package ua.com.fielden.platform.eql.stage2.elements;

public class MinuteOf2 extends SingleOperandFunction2 {

    public MinuteOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public Class type() {
        return Integer.class;
    }
}