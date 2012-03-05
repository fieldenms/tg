package ua.com.fielden.platform.entity.query.generation.elements;


public class SecondOfModel extends SingleOperandFunctionModel {

    public SecondOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "SECOND(" + getOperand().sql() + ")";
    }
}