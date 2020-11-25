package ua.com.fielden.platform.eql.stage3.operands;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;

public class OperandsBasedSet3 implements ISetOperand3 {
    private final List<ISingleOperand3> operands;

    public OperandsBasedSet3(final List<ISingleOperand3> operands) {
        this.operands = operands;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return format("(%s)", operands.stream().map(op -> op.sql(dbVersion)).collect(joining(", ")));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operands.hashCode();
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