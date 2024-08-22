package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Objects;

public class NullPredicate3 implements ICondition3 {
    public final ISingleOperand3 operand;
    private final boolean negated;

    public NullPredicate3(final ISingleOperand3 operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

    @Override
    public String sql(final IDomainMetadata metadata) {
        return operand.sql(metadata) + " IS " + (negated ? "NOT" : "") + " NULL";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NullPredicate3)) {
            return false;
        }
        final NullPredicate3 other = (NullPredicate3) obj;

        return (negated == other.negated) && Objects.equals(operand, other.operand);
    }

}
