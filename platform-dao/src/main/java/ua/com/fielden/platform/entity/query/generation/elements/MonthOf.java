package ua.com.fielden.platform.entity.query.generation.elements;


public class MonthOf extends SingleOperandFunction {

    public MonthOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "MONTH(" + getOperand().sql() + ")";
    }
}