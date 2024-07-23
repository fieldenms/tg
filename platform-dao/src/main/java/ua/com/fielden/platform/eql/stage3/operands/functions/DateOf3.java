package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class DateOf3 extends SingleOperandFunction3 {

    public DateOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }
    
    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
            return format("CAST(%s AS DATE)", operand.sql(dbVersion));
        case MSSQL:
            return format("DATEADD(dd, DATEDIFF(dd, 0, %s), 0)", operand.sql(dbVersion));
        case POSTGRESQL:
            return format("DATE_TRUNC('day', cast (%s as timestamp))", operand.sql(dbVersion));
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