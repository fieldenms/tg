package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class SecondOf3 extends SingleOperandFunction3 {

    public SecondOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
            return String.format("SECOND(%s)", operand.sql(metadata, dbVersion));
        case MSSQL:
            return String.format("DATEPART(ss, %s)", operand.sql(metadata, dbVersion));
        case POSTGRESQL:
            return String.format("CAST(EXTRACT(SECOND FROM %s \\:\\:timestamp) AS INT)", operand.sql(metadata, dbVersion));
        default:
            return super.sql(metadata, dbVersion);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SecondOf3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SecondOf3;
    }
}
