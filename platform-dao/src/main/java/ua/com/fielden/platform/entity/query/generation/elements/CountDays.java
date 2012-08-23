package ua.com.fielden.platform.entity.query.generation.elements;


public class CountDays extends TwoOperandsFunction {

    private String intervalUnit;

    public CountDays(final String intervalUnit, final ISingleOperand operand1, final ISingleOperand operand2) {
	super(operand1, operand2);
	this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql() {
	if ("DAY".equals(intervalUnit)) {
	    return "DATEDIFF('DAY', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	} else if ("MONTH".equals(intervalUnit)) {
	    return "DATEDIFF('MONTH', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	} else if ("YEAR".equals(intervalUnit)) {
	    return "DATEDIFF('YEAR', " + getOperand1().sql() + ", " + getOperand2().sql() + ")";
	}

	return null;
    }
}