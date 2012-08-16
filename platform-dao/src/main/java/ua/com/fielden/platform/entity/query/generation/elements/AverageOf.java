package ua.com.fielden.platform.entity.query.generation.elements;


public class AverageOf extends SingleOperandFunction {
    private final boolean distinct;
    public AverageOf(final ISingleOperand operand, final boolean distinct) {
	super(operand);
	this.distinct = distinct;
    }

    @Override
    public String sql() {
	return "AVG(" + (distinct ? "DISTINCT " : "") + "CAST (" + getOperand().sql() + " AS FLOAT))";
    }
}