package ua.com.fielden.platform.eql.stage3.elements.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class HourOf3 extends SingleOperandFunction3 {

    public HourOf3(final ISingleOperand3 operand) {
        super(operand);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
            return format("HOUR(%s)", operand.sql(dbVersion));
        case MSSQL:
            return format("DATEPART(hh, %s)", operand.sql(dbVersion));
        case POSTGRESQL:
            return format("CAST(EXTRACT(HOUR FROM %s) AS INT)", operand.sql(dbVersion));
        default:
            return super.sql(dbVersion);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + HourOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof HourOf3;
    }
}