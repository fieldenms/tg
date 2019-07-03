package ua.com.fielden.platform.eql.stage3.elements.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class MaxOf3 extends SingleOperandFunction3 {

    public MaxOf3(final ISingleOperand3 operand) {
        super(operand);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return format("MAX(%s)", operand.sql(dbVersion));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + MaxOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof MaxOf3;
    }
}