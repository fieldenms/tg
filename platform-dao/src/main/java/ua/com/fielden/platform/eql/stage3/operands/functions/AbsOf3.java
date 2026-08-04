package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class AbsOf3 extends SingleOperandFunction3 {

    public AbsOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }

    @Override
    public AbsOf3 setOperand(final ISingleOperand3 operand) {
        return operand == this.operand ? this : new AbsOf3(operand, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
        case MSSQL:
        case POSTGRESQL:
            return format("ABS(%s)", operand.sql(metadata, dbVersion));
        default:
            return super.sql(metadata, dbVersion);
        }
    }
}
