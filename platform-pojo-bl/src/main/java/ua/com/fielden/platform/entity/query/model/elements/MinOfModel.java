package ua.com.fielden.platform.entity.query.model.elements;


public class MinOfModel extends SingleOperandFunctionModel {

    public MinOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "MIN(" + getOperand().sql() + ")";
    }
}