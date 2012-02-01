package ua.com.fielden.platform.entity.query.generation.elements;


public class DayOfModel extends SingleOperandFunctionModel {

    public DayOfModel(final ISingleOperand operand) {
	super(operand);
    }

    @Override
    public String sql() {
	return "DAY(" + getOperand().sql() + ")";
    }

}