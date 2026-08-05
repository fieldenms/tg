package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import static java.lang.String.format;

public class AverageOf3 extends SingleOperandFunction3 {
    public final boolean distinct;
    
    public AverageOf3(final ISingleOperand3 operand, final boolean distinct, final PropType type) {
        super(operand, type);
        this.distinct = distinct;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String distinctClause = distinct ? "DISTINCT " : "";
        switch (dbVersion) {
        case H2:
            return String.format("AVG(%s CAST (%s AS FLOAT))", distinctClause, operand.sql(metadata, dbVersion));
        default:
            return String.format("AVG(%s %s)", distinctClause, operand.sql(metadata, dbVersion));
        }
    }
    
    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("distinct", distinct);
    }

}
