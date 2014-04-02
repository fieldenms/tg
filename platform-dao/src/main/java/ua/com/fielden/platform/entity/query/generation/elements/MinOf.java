package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.entity.query.DbVersion;

public class MinOf extends SingleOperandFunction {

    public MinOf(final ISingleOperand operand, final DbVersion dbVersion) {
        super(dbVersion, operand);
    }

    @Override
    public String sql() {
        return "MIN(" + getOperand().sql() + ")";
    }
}