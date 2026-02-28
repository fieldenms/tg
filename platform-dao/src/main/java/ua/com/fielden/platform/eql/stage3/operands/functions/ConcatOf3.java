package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static java.lang.String.format;

public class ConcatOf3 extends TwoOperandsFunction3 {

    public ConcatOf3(final ISingleOperand3 operand1, final ISingleOperand3 operand2, final PropType type) {
        super(operand1, operand2, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return format("STRING_AGG(%s, %s)", operand1.sql(metadata, dbVersion), operand2.sql(metadata, dbVersion));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ConcatOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ConcatOf3;
    }
}
