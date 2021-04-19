package ua.com.fielden.platform.eql.stage3.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class AbsOf3 extends SingleOperandFunction3 {

    public AbsOf3(final ISingleOperand3 operand, final Class<?> type, final Object hibType) {
        super(operand, type, hibType);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
        case MSSQL:
        case POSTGRESQL:
            return format("ABS(%s)", operand.sql(dbVersion));
        default:
            return super.sql(dbVersion);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + AbsOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof AbsOf3; 
    }
}