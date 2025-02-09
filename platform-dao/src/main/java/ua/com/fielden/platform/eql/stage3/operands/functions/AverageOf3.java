package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class AverageOf3 extends SingleOperandFunction3 {
    private final boolean distinct;
    
    public AverageOf3(final ISingleOperand3 operand, final boolean distinct, final PropType type) {
        super(operand, type);
        this.distinct = distinct;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String distinctClause = distinct ? "DISTINCT " : "";
        switch (dbVersion) {
        case H2:
            return format("AVG(%s CAST (%s AS FLOAT))", distinctClause, operand.sql(metadata, dbVersion));
        default:
            return format("AVG(%s %s)", distinctClause, operand.sql(metadata, dbVersion));
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + (distinct ? 1231 : 1237);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof AverageOf3)) {
            return false;
        }
        
        final AverageOf3 other = (AverageOf3) obj;
        
        return distinct == other.distinct;
    }
}
