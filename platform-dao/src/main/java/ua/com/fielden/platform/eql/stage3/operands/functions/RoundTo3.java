package ua.com.fielden.platform.eql.stage3.operands.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class RoundTo3 extends TwoOperandsFunction3 {

    public RoundTo3(final ISingleOperand3 operand1, final ISingleOperand3 operand2, final PropType type) {
        super(operand1, operand2, type);
    }

    @Override
    public String sql(final EqlDomainMetadata metadata) {
        return format("ROUND(%s, %s)", operand1.sql(metadata), operand2.sql(metadata));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + RoundTo3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof RoundTo3;
    }
}
