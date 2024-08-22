package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class DateOf3 extends SingleOperandFunction3 {

    public DateOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }
    
    @Override
    public String sql(final IDomainMetadata metadata) {
        switch (metadata.dbVersion()) {
        case H2:
            return format("CAST(%s AS DATE)", operand.sql(metadata));
        case MSSQL:
            return format("DATEADD(dd, DATEDIFF(dd, 0, %s), 0)", operand.sql(metadata));
        case POSTGRESQL:
            return format("DATE_TRUNC('day', cast (%s as timestamp))", operand.sql(metadata));
        default:
            return super.sql(metadata);
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
