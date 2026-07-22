package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class MinuteOf3 extends SingleOperandFunction3 {

    public MinuteOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return switch (dbVersion) {
            case H2 -> format("MINUTE(%s)", operand.sql(metadata, dbVersion));
            case MSSQL -> format("DATEPART(mi, %s)", operand.sql(metadata, dbVersion));
            case POSTGRESQL -> format("CAST(EXTRACT(MINUTE FROM %s \\:\\:timestamp) AS INT)", operand.sql(metadata, dbVersion));
            default -> super.sql(metadata, dbVersion);
        };
    }
}
