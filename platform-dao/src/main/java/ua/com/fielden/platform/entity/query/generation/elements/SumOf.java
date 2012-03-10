package ua.com.fielden.platform.entity.query.generation.elements;

public class SumOf extends SingleOperandFunction {
    private final boolean distinct;
    public SumOf(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }

    @Override
    public String sql() {
	return "SUM(" + (distinct ? "DISTINCT " : "") + getOperand().sql() + ")";
    }
}