package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.util.List;
import java.util.Objects;

public class OperandsBasedSet2 implements ISetOperand2 {
    private final List<ISingleOperand2> operands;

    public OperandsBasedSet2(final List<ISingleOperand2> operands) {
        this.operands = operands;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operands == null) ? 0 : operands.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OperandsBasedSet2)) {
            return false;
        }
        final OperandsBasedSet2 other = (OperandsBasedSet2) obj;
        
        return Objects.equals(operands, other.operands);
    }
}