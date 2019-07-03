package ua.com.fielden.platform.eql.stage3.elements.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class SumOf3 extends SingleOperandFunction3 {
    private final boolean distinct;
    
    public SumOf3(final ISingleOperand3 operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        final String distinctClause = distinct ? "DISTINCT " : "";
        return format("SUM(%s %s)", distinctClause, operand.sql(dbVersion));
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
        
        if (!(obj instanceof SumOf3)) {
            return false;
        }
        
        final SumOf3 other = (SumOf3) obj;
        
        return distinct == other.distinct;
    }
}