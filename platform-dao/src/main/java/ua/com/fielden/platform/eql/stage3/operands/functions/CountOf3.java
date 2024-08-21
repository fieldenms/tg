package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class CountOf3 extends SingleOperandFunction3 {
    private final boolean distinct;
    
    public CountOf3(final ISingleOperand3 operand, final boolean distinct, final PropType type) {
        super(operand, type);
        this.distinct = distinct;
    }
    
    @Override
    public String sql(final EqlDomainMetadata metadata) {
        final String distinctClause = distinct ? "DISTINCT " : "";
        return format("COUNT(%s %s)", distinctClause, operand.sql(metadata));
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
        
        if (!(obj instanceof CountOf3)) {
            return false;
        }
        
        final CountOf3 other = (CountOf3) obj;
        
        return distinct == other.distinct;
    }
}
