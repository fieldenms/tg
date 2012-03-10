package ua.com.fielden.platform.entity.query.generation.elements;


public class MinOf extends SingleOperandFunction {

    public MinOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "MIN(" + getOperand().sql() + ")";
    }
}