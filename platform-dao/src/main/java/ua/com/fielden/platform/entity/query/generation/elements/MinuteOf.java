package ua.com.fielden.platform.entity.query.generation.elements;


public class MinuteOf extends SingleOperandFunction {

    public MinuteOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "MINUTE(" + getOperand().sql() + ")";
    }
}