package ua.com.fielden.platform.entity.query.generation.elements;


public class SecondOf extends SingleOperandFunction {

    public SecondOf(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "SECOND(" + getOperand().sql() + ")";
    }
}