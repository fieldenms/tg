package ua.com.fielden.platform.eql.stage3.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class MinOf3 extends SingleOperandFunction3 {

    public MinOf3(final ISingleOperand3 operand) {
        super(operand);
    }
    
    @Override
    public Class<?> type() {
        return operand.type();
    }

    @Override
    public Object hibType() {
        return operand.hibType();
    } 

    @Override
    public String sql(final DbVersion dbVersion) {
        return format("MIN(%s)", operand.sql(dbVersion));
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