package ua.com.fielden.platform.eql.stage2.elements;

public class LowerCaseOf2 extends SingleOperandFunction2 {
    public LowerCaseOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public String type() {
        return String.class.getName();
    }
}