package ua.com.fielden.platform.eql.stage3.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class AddDateInterval3 extends TwoOperandsFunction3 {
    private final DateIntervalUnit intervalUnit;
    
    public AddDateInterval3(final ISingleOperand3 operand1, final DateIntervalUnit intervalUnit, final ISingleOperand3 operand2) {
        super(operand1, operand2);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
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