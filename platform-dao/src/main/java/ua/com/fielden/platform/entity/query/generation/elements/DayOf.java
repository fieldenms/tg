package ua.com.fielden.platform.entity.query.generation.elements;


public class DayOf extends SingleOperandFunction {

    public DayOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "DAY(" + getOperand().sql() + ")";
    }

}