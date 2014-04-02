package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.DbVersion;

public class SecondOf extends SingleOperandFunction {

    public SecondOf(final ISingleOperand operand, final DbVersion dbVersion) {
        super(dbVersion, operand);
    }

    @Override
    public String sql() {
        switch (getDbVersion()) {
        case H2:
            return "SECOND(" + getOperand().sql() + ")";
        case MSSQL:
            return "DATEPART(ss, " + getOperand().sql() + ")";
        case POSTGRESQL:
            return "CAST(EXTRACT(SECOND FROM " + getOperand().sql() + ") AS INT)";
        default:
            throw new IllegalStateException("Function [" + getClass().getSimpleName() + "] is not yet implemented for RDBMS [" + getDbVersion() + "]!");
        }
    }
}