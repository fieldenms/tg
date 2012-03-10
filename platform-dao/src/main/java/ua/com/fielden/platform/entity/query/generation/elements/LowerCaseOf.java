package ua.com.fielden.platform.entity.query.generation.elements;

public class LowerCaseOf extends SingleOperandFunction {
    public LowerCaseOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "LOWER(" + getOperand().sql() + ")";
    }
}