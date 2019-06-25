package ua.com.fielden.platform.eql.stage3.elements.operands;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Objects;

public class OperandsBasedSet3 implements ISetOperand3 {
    private final List<ISingleOperand3> operands;

    public OperandsBasedSet3(final List<ISingleOperand3> operands) {
        this.operands = operands;
    }

    @Override
    public String sql() {
        final StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append(operands.stream().map(op -> op.sql()).collect(joining(", ")));
        sb.append(")");
        return sb.toString();
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

        if (!(obj instanceof OperandsBasedSet3)) {
            return false;
        }
        final OperandsBasedSet3 other = (OperandsBasedSet3) obj;
        
        return Objects.equals(operands, other.operands);
    }
}