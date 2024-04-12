package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class HourOf3 extends SingleOperandFunction3 {

    public HourOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }

    @Override
    public String sql(final EqlDomainMetadata metadata) {
        switch (metadata.dbVersion) {
        case H2:
            return format("HOUR(%s)", operand.sql(metadata));
        case MSSQL:
            return format("DATEPART(hh, %s)", operand.sql(metadata));
        case POSTGRESQL:
            return format("CAST(EXTRACT(HOUR FROM %s \\:\\:timestamp) AS INT)", operand.sql(metadata));
        default:
            return super.sql(metadata);
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
