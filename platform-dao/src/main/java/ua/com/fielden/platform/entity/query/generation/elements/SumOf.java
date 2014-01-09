package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.DbVersion;

public class SumOf extends SingleOperandFunction {
    private final boolean distinct;
    public SumOf(final ISingleOperand operand, final boolean distinct, final DbVersion dbVersion) {
	super(dbVersion, operand);
	this.distinct = distinct;
    }

    @Override
    public String sql() {
	return "SUM(" + (distinct ? "DISTINCT " : "") + getOperand().sql() + ")";
    }
}