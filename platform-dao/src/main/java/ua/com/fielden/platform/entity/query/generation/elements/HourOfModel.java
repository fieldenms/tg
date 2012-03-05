package ua.com.fielden.platform.entity.query.generation.elements;


public class HourOfModel extends SingleOperandFunctionModel {

    public HourOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "HOUR(" + getOperand().sql() + ")";
    }
}