package ua.com.fielden.platform.eql.stage2.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class GroupBy2 {
    public final ISingleOperand2 operand;

    public GroupBy2(final ISingleOperand2 operand) {
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

        if (!(obj instanceof GroupBy2)) {
            return false;
        }
        
        final GroupBy2 other = (GroupBy2) obj;
        
        return Objects.equals(operand, other.operand);
    }
}
