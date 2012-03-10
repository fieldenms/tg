package ua.com.fielden.platform.entity.query.generation.elements;

public class UpperCaseOf extends SingleOperandFunction {
    public UpperCaseOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "UPPER(" + getOperand().sql() + ")";
    }
}