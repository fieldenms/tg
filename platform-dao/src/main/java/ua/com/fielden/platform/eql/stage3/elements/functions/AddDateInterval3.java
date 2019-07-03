package ua.com.fielden.platform.eql.stage3.elements.functions;

import static java.lang.String.format;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class AddDateInterval3 extends TwoOperandsFunction3 {
    private final DateIntervalUnit intervalUnit;
    
    public AddDateInterval3(final ISingleOperand3 operand1, final DateIntervalUnit intervalUnit, final ISingleOperand3 operand2) {
        super(operand1, operand2);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        switch (dbVersion) {
        case POSTGRESQL:
            return format("('1 %s' * %s + %s)",  intervalUnit, operand1.sql(dbVersion), operand2.sql(dbVersion));
        case H2:
            return format("DATEADD('%s', %s, %s)",  intervalUnit, operand1.sql(dbVersion), operand2.sql(dbVersion));
        case MSSQL:
            return format("DATEADD(%s, %s, %s)",  intervalUnit, operand1.sql(dbVersion), operand2.sql(dbVersion));
        default:
            return super.sql(dbVersion);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ((intervalUnit) == null ? 0 : intervalUnit.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof AddDateInterval3)) {
            return false;
        }
        
        final AddDateInterval3 other = (AddDateInterval3) obj;
        
        return Objects.equals(intervalUnit, other.intervalUnit);
    }
}