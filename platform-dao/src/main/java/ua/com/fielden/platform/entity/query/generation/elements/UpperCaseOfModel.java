package ua.com.fielden.platform.entity.query.generation.elements;

public class UpperCaseOfModel extends SingleOperandFunctionModel {
    public UpperCaseOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "UPPER(" + getOperand().sql() + ")";
    }
}