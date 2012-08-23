package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class AverageOf extends SingleOperandFunction {
    private final boolean distinct;
    public AverageOf(final ISingleOperand operand, final boolean distinct, final DbVersion dbVersion) {
	super(dbVersion, operand);
	this.distinct = distinct;
    }

    @Override
    public String sql() {
	return "AVG(" + (distinct ? "DISTINCT " : "") + "CAST (" + getOperand().sql() + " AS FLOAT))";
    }
}