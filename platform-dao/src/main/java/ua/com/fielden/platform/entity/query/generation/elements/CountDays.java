package ua.com.fielden.platform.entity.query.generation.elements;


public class CountDays extends TwoOperandsFunction {

    public CountDays(final ISingleOperand operand1, final ISingleOperand operand2) {
	super(operand1, operand2);
    }

    @Override
    public String sql() {
	return "DATEDIFF('DAY', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
    }
}