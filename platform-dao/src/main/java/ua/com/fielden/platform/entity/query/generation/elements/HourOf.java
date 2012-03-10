package ua.com.fielden.platform.entity.query.generation.elements;


public class HourOf extends SingleOperandFunction {

    public HourOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "HOUR(" + getOperand().sql() + ")";
    }
}