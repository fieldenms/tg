package ua.com.fielden.platform.entity.query.model.elements;


public class AverageOfModel extends SingleOperandFunctionModel {

    public AverageOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "AVG(" + getOperand().sql() + ")";
    }
}