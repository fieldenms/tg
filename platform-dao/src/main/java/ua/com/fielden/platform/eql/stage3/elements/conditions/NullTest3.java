package ua.com.fielden.platform.eql.stage3.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class NullTest3 implements ICondition3 {
    public final ISingleOperand3 operand;
    private final boolean negated;

    public NullTest3(final ISingleOperand3 operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NullTest3)) {
            return false;
        }
        final NullTest3 other = (NullTest3) obj;

        return Objects.equals(negated, other.negated) && Objects.equals(operand, other.operand);
    }
}