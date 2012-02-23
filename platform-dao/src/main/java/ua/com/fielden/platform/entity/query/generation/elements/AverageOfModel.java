package ua.com.fielden.platform.entity.query.generation.elements;


public class AverageOfModel extends SingleOperandFunctionModel {
    private final boolean distinct;
    public AverageOfModel(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }

    @Override
    public String sql() {
	return "AVG(" + (distinct ? "DISTINCT " : "") + getOperand().sql() + ")";
    }
}