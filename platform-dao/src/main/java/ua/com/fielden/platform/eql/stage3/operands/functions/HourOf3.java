package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class HourOf3 extends SingleOperandFunction3 {

    public HourOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return switch (dbVersion) {
            case H2 -> format("HOUR(%s)", operand.sql(metadata, dbVersion));
            case MSSQL -> format("DATEPART(hh, %s)", operand.sql(metadata, dbVersion));
            case POSTGRESQL -> format("CAST(EXTRACT(HOUR FROM %s \\:\\:timestamp) AS INT)", operand.sql(metadata, dbVersion));
            default -> super.sql(metadata, dbVersion);
        };
    }
}
