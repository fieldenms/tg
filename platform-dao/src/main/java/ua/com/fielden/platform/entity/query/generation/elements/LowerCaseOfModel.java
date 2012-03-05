package ua.com.fielden.platform.entity.query.generation.elements;

public class LowerCaseOfModel extends SingleOperandFunctionModel {
    public LowerCaseOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "LOWER(" + getOperand().sql() + ")";
    }
}