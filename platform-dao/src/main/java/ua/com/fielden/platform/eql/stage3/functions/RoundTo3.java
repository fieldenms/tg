package ua.com.fielden.platform.eql.stage3.functions;

import static java.lang.String.format;

import java.math.BigDecimal;

import org.hibernate.type.BigDecimalType;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class RoundTo3 extends TwoOperandsFunction3 {

    public RoundTo3(final ISingleOperand3 operand1, final ISingleOperand3 operand2) {
        super(operand1, operand2);
    }

    @Override
    public Class<BigDecimal> type() {
        return BigDecimal.class; //TODO
    }

    @Override
    public Object hibType() {
        return BigDecimalType.INSTANCE;
    }
    
    @Override
    public String sql(final DbVersion dbVersion) {
        return format("ROUND(%s, %s)", operand1.sql(dbVersion), operand2.sql(dbVersion));
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