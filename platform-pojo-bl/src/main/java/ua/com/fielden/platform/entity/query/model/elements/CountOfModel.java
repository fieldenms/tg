package ua.com.fielden.platform.entity.query.model.elements;


public class CountOfModel extends SingleOperandFunctionModel {

    public CountOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "COUNT(" + getOperand().sql() + ")";
    }

}