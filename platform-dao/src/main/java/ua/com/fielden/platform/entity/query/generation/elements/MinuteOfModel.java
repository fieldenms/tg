package ua.com.fielden.platform.entity.query.generation.elements;


public class MinuteOfModel extends SingleOperandFunctionModel {

    public MinuteOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "MINUTE(" + getOperand().sql() + ")";
    }
}