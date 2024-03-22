package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class Concat3 extends AbstractFunction3 {

    private final List<ISingleOperand3> operands;

    public Concat3(final List<ISingleOperand3> operands, final PropType type) {
        super(type);
        this.operands = operands;
    }

    @Override
    public String sql(final EqlDomainMetadata metadata) {
        switch (metadata.dbVersion) {
        case H2:
        case MSSQL:
            return format(" (%s)", operands.stream().map(so -> getConvertToStringSql(metadata, so)).collect(joining(" + ")));
        case POSTGRESQL:
            return format(" (%s)", operands.stream().map(so -> getConvertToStringSql(metadata, so)).collect(joining(" || ")));
        default:
            return super.sql(metadata);
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
        if (this == obj) {
            return true;
        }
    
        if (!(obj instanceof Concat3)) {
            return false;
        }
        
        final Concat3 other = (Concat3) obj;
        
        return Objects.equals(operands, other.operands);
    }
}
