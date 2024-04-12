package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class MinOf3 extends SingleOperandFunction3 {

    public MinOf3(final ISingleOperand3 operand, final PropType type) {
        super(operand, type);
    }
    
    @Override
    public String sql(final EqlDomainMetadata metadata) {
        return format("MIN(%s)", operand.sql(metadata));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + MinOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof MinOf3;
    } 
}
