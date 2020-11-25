package ua.com.fielden.platform.eql.stage3.functions;

import static java.lang.String.format;

import java.util.Date;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.persistence.types.DateTimeType;

public class DateOf3 extends SingleOperandFunction3 {

    public DateOf3(final ISingleOperand3 operand) {
        super(operand);
    }
    
    @Override
    public Class<Date> type() {
        return Date.class; // TODO
    }

    @Override
    public Object hibType() {
        return new DateTimeType();
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
            return format("CAST(%s AS DATE)", operand.sql(dbVersion));
        case MSSQL:
            return format("DATEADD(dd, DATEDIFF(dd, 0, %s), 0)", operand.sql(dbVersion));
        case POSTGRESQL:
            return format("DATE_TRUNC('day', %s)", operand.sql(dbVersion));
        default:
            return super.sql(dbVersion);
        }    
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + DateOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof DateOf3;
    }
}