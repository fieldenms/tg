package ua.com.fielden.platform.entity.query.generation.elements;


public class CountOf extends SingleOperandFunction {
    private final boolean distinct;
    public CountOf(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }

    @Override
    public String sql() {
	return "COUNT(" + (distinct ? "DISTINCT " : "") + getOperand().sql() + ")";
    }

}