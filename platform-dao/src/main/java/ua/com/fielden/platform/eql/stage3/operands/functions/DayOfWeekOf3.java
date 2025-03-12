package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class DayOfWeekOf3 extends SingleOperandFunction3 {

    public DayOfWeekOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }
    
    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return switch (dbVersion) {
            case H2 -> format("ISO_DAY_OF_WEEK(%s)", operand.sql(metadata, dbVersion));
            case MSSQL -> {
                final String operandSql = operand.sql(metadata, dbVersion);
                yield format("((DATEPART(DW, %s) + @@DATEFIRST - 1) %% 8 + (DATEPART(DW, %s) + @@DATEFIRST - 1) / 8)", operandSql, operandSql);
            }
            case POSTGRESQL ->
                // need to typecast explicitly to allow usage of date literals
                // TODO differentiate between date literals and date columns – only date literals need to be typecasted explicitly.
                    format("CAST(EXTRACT(ISODOW FROM %s \\:\\:timestamp) AS INT)", operand.sql(metadata, dbVersion));
            default -> super.sql(metadata, dbVersion);
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + DayOfWeekOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof DayOfWeekOf3 && super.equals(obj); 
    }

}
