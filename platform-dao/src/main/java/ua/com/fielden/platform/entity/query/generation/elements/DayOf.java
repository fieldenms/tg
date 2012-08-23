package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class DayOf extends SingleOperandFunction {

    public DayOf(final ISingleOperand operand, final DbVersion dbVersion) {
	super(dbVersion, operand);
    }

    @Override
    public String sql() {
	switch (getDbVersion()) {
	case H2:
	    return "DAY(" + getOperand().sql() + ")";
	case POSTGRESQL:
	    return "CAST(EXTRACT(DAY FROM " + getOperand().sql() + ") AS INT)";
	default:
	    return null;
	}
    }
}