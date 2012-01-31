package ua.com.fielden.platform.entity.query.model.elements;


public class MaxOfModel extends SingleOperandFunctionModel {

    public MaxOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "MAX(" + getOperand().sql() + ")";
    }

}