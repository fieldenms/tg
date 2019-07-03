package ua.com.fielden.platform.eql.stage3.elements.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class DayOfWeekOf3 extends SingleOperandFunction3 {

    public DayOfWeekOf3(final ISingleOperand3 operand) {
        super(operand);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
            return format("ISO_DAY_OF_WEEK(%s)", operand.sql(dbVersion));
        case MSSQL:
            final String operandSql = operand.sql(dbVersion);
            return format("((DATEPART(DW, %s) + @@DATEFIRST - 1) %% 8 + (DATEPART(DW, %s) + @@DATEFIRST - 1) / 8)", operandSql, operandSql);
        case POSTGRESQL:
            return format("CAST(EXTRACT(ISODOW FROM %s) AS INT)", operand.sql(dbVersion));
        default:
            return super.sql(dbVersion);
        }   
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + DayOfWeekOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof DayOfWeekOf3; 
    }
}
