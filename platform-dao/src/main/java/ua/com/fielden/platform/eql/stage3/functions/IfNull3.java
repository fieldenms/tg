package ua.com.fielden.platform.eql.stage3.functions;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class IfNull3 extends TwoOperandsFunction3 {

    public IfNull3(final ISingleOperand3 operand1, final ISingleOperand3 operand2, final Class<?> type, final Object hibType) {
        super(operand1, operand2, type, hibType);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return format("COALESCE(%s, %s)", operand1.sql(dbVersion), operand2.sql(dbVersion));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + IfNull3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof IfNull3;
    } 
}