package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.stage3.utils.OperandToSqlAsString.operandToSqlAsString;

public class Concat3 extends AbstractFunction3 {

    private final List<ISingleOperand3> operands;

    public Concat3(final List<ISingleOperand3> operands, final PropType type) {
        super(type);
        this.operands = operands;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        switch (dbVersion) {
        case H2:
        case MSSQL:
            return format(" (%s)", operands.stream().map(so -> operandToSqlAsString(metadata, dbVersion, so)).collect(joining(" + ")));
        case POSTGRESQL:
            return format(" (%s)", operands.stream().map(so -> operandToSqlAsString(metadata, dbVersion, so)).collect(joining(" || ")));
        default:
            return super.sql(metadata, dbVersion);
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + operands.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof Concat3 that
                  && Objects.equals(this.operands, that.operands);
    }
}
