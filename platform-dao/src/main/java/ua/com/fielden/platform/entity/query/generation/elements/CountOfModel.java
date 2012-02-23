package ua.com.fielden.platform.entity.query.generation.elements;


public class CountOfModel extends SingleOperandFunctionModel {
    private final boolean distinct;
    public CountOfModel(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }

    @Override
    public String sql() {
	return "COUNT(" + (distinct ? "DISTINCT " : "") + getOperand().sql() + ")";
    }

}