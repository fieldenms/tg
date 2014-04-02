package ua.com.fielden.platform.eql.s2.elements;

public class MinOf2 extends SingleOperandFunction2 {

    public MinOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public Class type() {
        return getOperand().type();
    }
}