package ua.com.fielden.platform.entity.query.generation.elements;


public class YearOf extends SingleOperandFunction {

    public YearOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "YEAR(" + getOperand().sql() + ")";
    }
}