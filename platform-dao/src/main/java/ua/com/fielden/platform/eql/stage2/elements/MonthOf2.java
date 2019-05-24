package ua.com.fielden.platform.eql.stage2.elements;

public class MonthOf2 extends SingleOperandFunction2 {

    public MonthOf2(final ISingleOperand2 operand) {
        super(operand);
    }

    @Override
    public String type() {
        return Integer.class.getName();
    }
}