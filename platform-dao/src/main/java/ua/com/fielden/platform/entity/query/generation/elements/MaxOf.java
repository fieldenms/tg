package ua.com.fielden.platform.entity.query.generation.elements;


public class MaxOf extends SingleOperandFunction {

    public MaxOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "MAX(" + getOperand().sql() + ")";
    }

}