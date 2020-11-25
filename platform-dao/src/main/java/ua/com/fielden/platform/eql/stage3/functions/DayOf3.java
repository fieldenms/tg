package ua.com.fielden.platform.eql.stage3.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class DayOf3 extends DatePartFunction3 {

    public DayOf3(final ISingleOperand3 operand) {
        super(operand);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
        case MSSQL:
            return format("DAY(%s)", operand.sql(dbVersion));
        case POSTGRESQL:
            return format("CAST(EXTRACT(DAY FROM %s) AS INT)", operand.sql(dbVersion));
        default:
            return super.sql(dbVersion);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + DayOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof DayOf3; 
    }
}