package ua.com.fielden.platform.eql.stage3.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class GroupBy3 {
    public final ISingleOperand3 operand;

    public GroupBy3(final ISingleOperand3 operand) {
        this.operand = operand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof GroupBy3)) {
            return false;
        }
        
        final GroupBy3 other = (GroupBy3) obj;
        
        return Objects.equals(operand, other.operand);
    }
}
