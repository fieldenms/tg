package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class MonthOf3 extends SingleOperandFunction3 {

    public MonthOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return switch (dbVersion) {
            case H2, MSSQL -> format("MONTH(%s)", operand.sql(metadata, dbVersion));
            case POSTGRESQL -> format("CAST(EXTRACT(MONTH FROM %s \\:\\:timestamp) AS INT)", operand.sql(metadata, dbVersion));
            default -> super.sql(metadata, dbVersion);
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + MonthOf3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof MonthOf3;
    }
}
